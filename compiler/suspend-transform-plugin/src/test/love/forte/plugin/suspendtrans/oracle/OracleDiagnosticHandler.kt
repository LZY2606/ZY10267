package love.forte.plugin.suspendtrans.oracle

import org.jetbrains.kotlin.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.diagnostics.impl.BaseDiagnosticsCollector
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.test.backend.handlers.JvmBinaryArtifactHandler
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.model.BinaryArtifacts
import org.jetbrains.kotlin.test.model.JvmClassFileArtifact
import org.jetbrains.kotlin.test.model.TestFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices

/**
 * Source-diagnostic oracle layer.
 *
 * Runs at the end of the compilation pipeline so that diagnostics from every
 * stage (resolution, JVM checkers, backend) are visible. Asserts ERROR
 * diagnostics as `FACTORY_NAME@line:col-line:col` pairs against the
 * `ORACLE_DIAGNOSTIC` directive. Diagnostic ids and source ranges are
 * compared, never the rendered message text. When the directive is absent,
 * no ERROR diagnostics are expected at all.
 */
class OracleDiagnosticHandler(testServices: TestServices) : JvmBinaryArtifactHandler(testServices) {
    override val directiveContainers: List<DirectivesContainer>
        get() = listOf(OracleDirectives)

    override fun processAfterAllModules(fromBindings: Boolean) {}

    override fun processModule(module: TestModule, info: BinaryArtifacts.Jvm) {
        val generationState = (info as JvmClassFileArtifact).classFileFactory.generationState
        val diagnosticsByFile =
            (generationState.diagnosticReporter as BaseDiagnosticsCollector).diagnosticsByFile

        val actual = sortedSetOf<String>()
        for ((sourceFile, diagnostics) in diagnosticsByFile) {
            val testFile = module.files.firstOrNull { it.originalFile.name == sourceFile?.name }
                ?: module.files.firstOrNull { !it.isAdditional }
            for (diagnostic in diagnostics) {
                if (diagnostic.severity != Severity.ERROR) continue
                actual += render(diagnostic, testFile)
            }
        }

        val expected = module.directives[OracleDirectives.ORACLE_DIAGNOSTIC]
            .flatMap { it.split(',') }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSortedSet()

        if (actual != expected) {
            throw AssertionError(
                "Diagnostic oracle mismatch.\n" +
                    "Expected (id@range):\n" + expected.joinToString("\n") { "  $it" } + "\n" +
                    "Actual (id@range):\n" + actual.joinToString("\n") { "  $it" } + "\n" +
                    "Declare them via // ORACLE_DIAGNOSTIC: ID@line:col-line:col, ..."
            )
        }
    }

    private fun render(diagnostic: KtDiagnostic, testFile: TestFile?): String {
        val range = runCatching { diagnostic.firstRange }.getOrNull()
        if (range == null || testFile == null) {
            return "${diagnostic.factoryName}@none"
        }
        val content = testFile.originalContent
        return "${diagnostic.factoryName}@${position(content, range.startOffset)}-${position(content, range.endOffset)}"
    }

    private fun position(content: String, offset: Int): String {
        val safeOffset = offset.coerceIn(0, content.length)
        var line = 1
        var lineStart = 0
        var index = 0
        while (index < safeOffset) {
            if (content[index] == '\n') {
                line++
                lineStart = index + 1
            }
            index++
        }
        return "$line:${safeOffset - lineStart + 1}"
    }
}
