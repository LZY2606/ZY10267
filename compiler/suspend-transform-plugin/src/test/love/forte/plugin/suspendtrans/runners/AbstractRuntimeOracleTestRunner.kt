package love.forte.plugin.suspendtrans.runners

import love.forte.plugin.suspendtrans.services.SuspendTransformerRuntimeClasspathProvider
import org.jetbrains.kotlin.test.backend.BlackBoxCodegenSuppressor
import org.jetbrains.kotlin.test.backend.handlers.JvmBoxRunner
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureJvmArtifactsHandlersStep
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives

/**
 * Runtime layer of the oracle.
 *
 * Each sample provides a `box()` function that reflects the generated bridge members,
 * invokes them and records which implementation was actually dispatched to. A sample
 * fails only when the observed runtime behavior diverges, never because of the compile
 * state alone.
 */
abstract class AbstractRuntimeOracleTestRunner : AbstractOracleTestRunner() {
    override fun TestConfigurationBuilder.configureOracleHandlers() {
        useDirectives(JvmEnvironmentConfigurationDirectives)
        useFailureSuppressors(::BlackBoxCodegenSuppressor)
        useCustomRuntimeClasspathProviders(::SuspendTransformerRuntimeClasspathProvider)

        configureJvmArtifactsHandlersStep {
            useHandlers(::JvmBoxRunner)
        }
    }
}
