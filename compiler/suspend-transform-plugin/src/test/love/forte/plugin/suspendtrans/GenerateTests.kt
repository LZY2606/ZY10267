package love.forte.plugin.suspendtrans
import love.forte.plugin.suspendtrans.runners.AbstractAbiOracleTestRunner
import love.forte.plugin.suspendtrans.runners.AbstractCodeGenTestRunner
import love.forte.plugin.suspendtrans.runners.AbstractDiagnosticOracleTestRunner
import love.forte.plugin.suspendtrans.runners.AbstractRuntimeOracleTestRunner
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5

fun main() {
    println("generating test class...")
    generateTestGroupSuiteWithJUnit5 {
        // Paths are relative to this module's project directory, which is both the
        // working directory of the `generateTest` JavaExec task and of the `test` task.
        testGroup(testsRoot = "src/test-gen", testDataRoot = "src/testData") {
            testClass<AbstractCodeGenTestRunner> {
                model(relativeRootPath = "codegen")
            }

            // Layer 1: source diagnostics oracle (diagnostic id + source range)
            testClass<AbstractDiagnosticOracleTestRunner> {
                model(relativeRootPath = "diagnostics")
            }

            // Layer 2: normalized ABI signature oracle, on the same minimal sources
            testClass<AbstractAbiOracleTestRunner> {
                model(relativeRootPath = "runtime")
            }

            // Layer 3: runtime dispatch oracle
            testClass<AbstractRuntimeOracleTestRunner> {
                model(relativeRootPath = "runtime")
            }
        }
    }
}
