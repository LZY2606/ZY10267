package love.forte.plugin.suspendtrans.runners

import love.forte.plugin.suspendtrans.services.NormalizedAbiSignatureHandler
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureJvmArtifactsHandlersStep

/**
 * ABI layer of the oracle.
 *
 * Compares only the normalized, order-insensitive public signature set of the generated
 * classes against `<testName>.abi.txt` golden files. Bytecode text, member order and
 * compiler temporary paths are not part of the oracle.
 */
abstract class AbstractAbiOracleTestRunner : AbstractOracleTestRunner() {
    override fun TestConfigurationBuilder.configureOracleHandlers() {
        configureJvmArtifactsHandlersStep {
            useHandlers(::NormalizedAbiSignatureHandler)
        }
    }
}
