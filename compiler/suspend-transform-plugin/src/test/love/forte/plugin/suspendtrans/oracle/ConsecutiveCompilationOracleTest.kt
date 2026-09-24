package love.forte.plugin.suspendtrans.oracle

import org.junit.jupiter.api.Test

/**
 * Compiles two related sources back to back in the same test JVM. The second
 * source declares the same class without any mark annotation; its ABI oracle
 * pins the exact public member set, so a generated member leaking from the
 * previous compilation (daemon/cache residue) fails the test.
 */
class ConsecutiveCompilationOracleTest : AbstractOracleTestRunner() {
    @Test
    fun testGeneratedMembersDoNotLeakAcrossConsecutiveCompilations() {
        runTest("src/testData/oracleIsolation/annotated.kt")
        runTest("src/testData/oracleIsolation/plain.kt")
        runTest("src/testData/oracleIsolation/annotated.kt")
    }
}
