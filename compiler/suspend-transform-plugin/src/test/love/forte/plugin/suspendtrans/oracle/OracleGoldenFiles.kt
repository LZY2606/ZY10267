package love.forte.plugin.suspendtrans.oracle

import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.AssertionsService
import java.io.File

/**
 * Compares oracle output with checked-in golden files that live next to the
 * test data source (`<name>.diag.txt` is not used; diagnostics expectations
 * are inline directives, ABI and runtime results use `<name>.abi.txt` and
 * `<name>.run.txt`).
 *
 * Golden files never contain compiler temporary paths or full diagnostic
 * messages, only normalized, sorted content. Setting
 * `-Dsuspendtrans.oracle.updateGoldens=true` rewrites them.
 */
internal object OracleGoldenFiles {
    private const val UPDATE_PROPERTY = "suspendtrans.oracle.updateGoldens"

    fun goldenFileFor(module: TestModule, suffix: String): File {
        val source = module.files.first { !it.isAdditional }.originalFile
        return File(source.parentFile, source.nameWithoutExtension + suffix)
    }

    fun assertMatches(assertions: AssertionsService, goldenFile: File, actualLines: List<String>) {
        val actual = actualLines.joinToString("\n").trimEnd() + "\n"
        if (System.getProperty(UPDATE_PROPERTY)?.toBoolean() == true) {
            goldenFile.writeText(actual)
            return
        }
        if (!goldenFile.exists()) {
            goldenFile.writeText(actual)
            throw AssertionError(
                "Golden file ${goldenFile.path} did not exist; it was created with the actual content. " +
                    "Review it and re-run the test."
            )
        }
        val expected = goldenFile.readText()
        assertions.assertEquals(expected, actual) {
            "Oracle output does not match golden file ${goldenFile.path}. " +
                "Re-run with -D$UPDATE_PROPERTY=true only after reviewing the diff."
        }
    }
}
