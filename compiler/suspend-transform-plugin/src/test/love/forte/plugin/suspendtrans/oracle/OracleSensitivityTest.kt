package love.forte.plugin.suspendtrans.oracle

import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Proves the runtime oracle layer is sensitive to dispatch bugs: the fixture
 * compiles cleanly, but its golden file deliberately expects the bridge to
 * dispatch to the wrong (base) implementation. The run must be rejected by
 * the runtime oracle, not by any compile-time check.
 */
class OracleSensitivityTest : AbstractOracleTestRunner() {
    @Test
    fun testWrongDispatchReceiverIsCaughtByRuntimeOracle() {
        val failure = assertFailsWith<AssertionError> {
            runTest("src/testData/oracleSensitivity/wrongDispatch.kt")
        }
        assertTrue(
            ".run.txt" in failure.message.orEmpty(),
            "Expected the runtime oracle (.run.txt) to reject the wrong dispatch, " +
                "but failed with: ${failure.message}",
        )
    }
}
