package love.forte.plugin.suspendtrans.oracle

import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.jetbrains.kotlin.test.backend.handlers.JvmBinaryArtifactHandler
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.JvmClassFileArtifact
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import org.reactivestreams.Publisher
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeUnit

/**
 * Runtime oracle layer.
 *
 * Invokes every member listed in `ORACLE_CALL` reflectively against classes
 * loaded in a fresh in-memory class loader, unwraps blocking/async/reactive
 * bridge results and records the value the call actually dispatched to.
 * Results are compared with `<test>.run.txt`, so a bridge that compiles but
 * dispatches to the wrong implementation is caught here.
 */
class OracleRuntimeHandler(testServices: TestServices) : JvmBinaryArtifactHandler(testServices) {
    override val directiveContainers: List<DirectivesContainer>
        get() = listOf(OracleDirectives)

    override fun processAfterAllModules(fromBindings: Boolean) {}

    override fun processModule(module: TestModule, info: BinaryArtifacts.Jvm) {
        val calls = module.directives[OracleDirectives.ORACLE_CALL]
            .flatMap { splitTopLevel(it) }
            .map { parseCall(it) }
        if (calls.isEmpty()) return

        val factory = (info as JvmClassFileArtifact).classFileFactory
        val loader = InMemoryClassLoader.from(factory, javaClass.classLoader)
        val lines = calls.sortedBy { it.label }.map { call ->
            "${call.label} -> ${invoke(call, loader)}"
        }

        OracleGoldenFiles.assertMatches(
            testServices.assertions,
            OracleGoldenFiles.goldenFileFor(module, ".run.txt"),
            lines,
        )
    }

    private fun invoke(call: RuntimeCall, loader: ClassLoader): String {
        val clazz = runCatching { Class.forName(call.className, true, loader) }.getOrElse {
            throw AssertionError("ORACLE_CALL class '${call.className}' was not generated: ${it.message}")
        }
        val method = clazz.methods.firstOrNull { candidate ->
            candidate.name == call.methodName && candidate.parameterCount == call.args.size
        } ?: throw AssertionError(
            "ORACLE_CALL member '${call.className}#${call.methodName}' with ${call.args.size} " +
                "parameter(s) was not generated. Available: " +
                clazz.methods.joinToString { "${it.name}/${it.parameterCount}" }
        )
        val target = if (Modifier.isStatic(method.modifiers)) {
            null
        } else {
            runCatching { clazz.getDeclaredConstructor().newInstance() }.getOrElse {
                throw AssertionError("ORACLE_CALL class '${call.className}' needs a public no-arg constructor: ${it.message}")
            }
        }
        val arguments = call.args.mapIndexed { index, spec -> spec.toValue(method.parameterTypes[index]) }
        val result = runCatching { method.invoke(target, *arguments.toTypedArray()) }.getOrElse {
            throw AssertionError("ORACLE_CALL '${call.label}' failed to invoke: ${it.cause ?: it}")
        }
        return renderResult(result)
    }

    private fun renderResult(result: Any?): String = when (result) {
        null -> "null"
        is CompletionStage<*> -> renderAsync { result.toCompletableFuture().get(60, TimeUnit.SECONDS) }
        is Publisher<*> -> renderAsync {
            runBlocking { withTimeout(60_000) { result.awaitFirstOrNull() } }
        }
        else -> result.toString()
    }

    private fun renderAsync(await: () -> Any?): String = try {
        await().toString()
    } catch (e: Exception) {
        val cause = e.cause ?: e
        "!<${cause.javaClass.simpleName}: ${cause.message}>"
    }

    private fun splitTopLevel(value: String): List<String> {
        val entries = mutableListOf<String>()
        var depth = 0
        var inString = false
        var start = 0
        value.forEachIndexed { index, c ->
            when {
                c == '\'' -> inString = !inString
                inString -> Unit
                c == '(' -> depth++
                c == ')' -> depth--
                c == ',' && depth == 0 -> {
                    entries += value.substring(start, index).trim()
                    start = index + 1
                }
            }
        }
        entries += value.substring(start).trim()
        return entries.filter { it.isNotEmpty() }
    }

    private fun parseCall(spec: String): RuntimeCall {
        val classEnd = spec.indexOf('#')
        require(classEnd > 0) { "ORACLE_CALL entry '$spec' must look like fq.Class#member(args)" }
        val className = spec.substring(0, classEnd)
        val rest = spec.substring(classEnd + 1)
        val paren = rest.indexOf('(')
        if (paren < 0) {
            return RuntimeCall(className, rest, emptyList())
        }
        require(rest.endsWith(")")) { "ORACLE_CALL entry '$spec' has malformed arguments" }
        val methodName = rest.substring(0, paren)
        val args = rest.substring(paren + 1, rest.lastIndex)
            .split(',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { ArgSpec(it) }
        return RuntimeCall(className, methodName, args)
    }

    private class RuntimeCall(
        val className: String,
        val methodName: String,
        val args: List<ArgSpec>,
    ) {
        val label: String
            get() = "$className#$methodName(${args.joinToString(", ") { it.raw }})"
    }

    private class ArgSpec(val raw: String) {
        fun toValue(parameterType: Class<*>): Any? = when {
            raw == "null" -> null
            raw == "true" -> true
            raw == "false" -> false
            raw.startsWith("'") && raw.endsWith("'") -> raw.substring(1, raw.length - 1)
            raw.toIntOrNull() != null -> when (parameterType) {
                java.lang.Long.TYPE, java.lang.Long::class.java -> raw.toLong()
                else -> raw.toInt()
            }
            else -> throw IllegalArgumentException("Unsupported ORACLE_CALL argument '$raw'")
        }
    }
}
