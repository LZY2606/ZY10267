package love.forte.plugin.suspendtrans
import love.forte.plugin.suspendtrans.oracle.AbstractOracleTestRunner
import love.forte.plugin.suspendtrans.runners.AbstractCodeGenTestRunner
import org.jetbrains.kotlin.generators.dsl.junit5.generateTestGroupSuiteWithJUnit5

fun main() {
    println("generating test class...")
    generateTestGroupSuiteWithJUnit5 {
        // Paths are relative to the module project directory, which is the
        // working directory of both the `generateTest` and `test` tasks.
        testGroup(testsRoot = "src/test-gen", testDataRoot = "src/testData") {
            testClass<AbstractCodeGenTestRunner> {
                model(relativeRootPath = "codegen")
            }

            testClass<AbstractOracleTestRunner> {
                model(relativeRootPath = "oracle")
            }

        }
    }
}
