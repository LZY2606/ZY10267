package love.forte.plugin.suspendtrans.oracle

import org.jetbrains.kotlin.test.backend.handlers.JvmBinaryArtifactHandler
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.JvmClassFileArtifact
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * ABI oracle layer.
 *
 * Dumps the normalized public member set (and Kotlin metadata version) of
 * every class listed in `ORACLE_ABI` and compares it with `<test>.abi.txt`.
 * Members are rendered as erased signatures and sorted, so unrelated
 * declaration reordering in generated output does not break the oracle while
 * the public member set and metadata stay pinned.
 */
class OracleAbiHandler(testServices: TestServices) : JvmBinaryArtifactHandler(testServices) {
    override val directiveContainers: List<DirectivesContainer>
        get() = listOf(OracleDirectives)

    override fun processAfterAllModules(fromBindings: Boolean) {}

    override fun processModule(module: TestModule, info: BinaryArtifacts.Jvm) {
        val classNames = module.directives[OracleDirectives.ORACLE_ABI]
            .flatMap { it.split(',') }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (classNames.isEmpty()) return

        val factory = (info as JvmClassFileArtifact).classFileFactory
        val loader = InMemoryClassLoader.from(factory, javaClass.classLoader)
        val lines = mutableListOf<String>()
        for (className in classNames.sorted()) {
            val clazz = runCatching { Class.forName(className, false, loader) }.getOrElse {
                throw AssertionError("ORACLE_ABI class '$className' was not generated: ${it.message}")
            }
            lines += "class $className"
            lines += "  metadata ${renderMetadata(clazz)}"
            val members = buildList {
                clazz.declaredConstructors
                    .filter { Modifier.isPublic(it.modifiers) }
                    .forEach { add("  ctor ${render(it)}") }
                clazz.declaredMethods
                    .filter { Modifier.isPublic(it.modifiers) }
                    .forEach { add("  fun ${render(it)}") }
                clazz.declaredFields
                    .filter { Modifier.isPublic(it.modifiers) }
                    .forEach { add("  field ${render(it)}") }
            }
            lines += members.sorted()
        }

        OracleGoldenFiles.assertMatches(
            testServices.assertions,
            OracleGoldenFiles.goldenFileFor(module, ".abi.txt"),
            lines,
        )
    }

    private fun renderMetadata(clazz: Class<*>): String {
        val metadata = clazz.getAnnotation(Metadata::class.java)
            ?: return "absent"
        return "k=${metadata.kind} mv=${metadata.metadataVersion.toList()} bv=${metadata.bytecodeVersion.toList()} xi=${metadata.extraInt}"
    }

    private fun renderModifiers(modifiers: Int, isMethod: Boolean): String = buildList {
        if (Modifier.isPublic(modifiers)) add("public")
        if (Modifier.isProtected(modifiers)) add("protected")
        if (Modifier.isPrivate(modifiers)) add("private")
        if (Modifier.isFinal(modifiers)) add("final")
        if (Modifier.isStatic(modifiers)) add("static")
        if (Modifier.isAbstract(modifiers)) add("abstract")
        if (Modifier.isSynchronized(modifiers)) add("synchronized")
        if (modifiers and ACC_BRIDGE_OR_VOLATILE != 0) add(if (isMethod) "bridge" else "volatile")
        if (Modifier.isTransient(modifiers)) add("transient")
        if (modifiers and ACC_SYNTHETIC != 0) add("synthetic")
    }.joinToString(" ")

    private fun render(method: Method): String {
        val params = method.parameterTypes.joinToString(", ") { it.canonicalName }
        return "${renderModifiers(method.modifiers, isMethod = true)} ${method.returnType.canonicalName} ${method.name}($params)"
    }

    private fun render(constructor: Constructor<*>): String {
        val params = constructor.parameterTypes.joinToString(", ") { it.canonicalName }
        return "${renderModifiers(constructor.modifiers, isMethod = true)} <init>($params)"
    }

    private fun render(field: Field): String =
        "${renderModifiers(field.modifiers, isMethod = false)} ${field.type.canonicalName} ${field.name}"

    private companion object {
        const val ACC_SYNTHETIC = 0x00001000
        const val ACC_BRIDGE_OR_VOLATILE = 0x00000040
    }
}
