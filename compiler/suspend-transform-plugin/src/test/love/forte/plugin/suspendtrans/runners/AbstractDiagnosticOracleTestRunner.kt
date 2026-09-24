package love.forte.plugin.suspendtrans.runners

import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureFirHandlersStep
import org.jetbrains.kotlin.test.builders.configureJvmArtifactsHandlersStep
import org.jetbrains.kotlin.test.directives.TestPhaseDirectives
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirDiagnosticsHandler
import org.jetbrains.kotlin.test.backend.handlers.JvmBackendDiagnosticsHandler
import org.jetbrains.kotlin.test.backend.handlers.UpdateTestDataHandler
import org.jetbrains.kotlin.test.services.PhasedPipelineChecker

/**
 * Source-diagnostic layer of the oracle.
 *
 * Failing samples declare expected diagnostics with `<!DIAGNOSTIC_ID!>` markers, so only
 * the diagnostic id and its source range are asserted, never the rendered message text.
 */
abstract class AbstractDiagnosticOracleTestRunner : AbstractOracleTestRunner() {
    override fun TestConfigurationBuilder.setupPipelineSteps() {
        // Expected-error samples must reach the diagnostics handler instead of the
        // compilation-error gates of the codegen pipeline.
        setupPipelineStepsWithoutCompilationErrorHandlers()
    }

    override fun TestConfigurationBuilder.configureOracleHandlers() {
        useDirectives(TestPhaseDirectives)
        useFailureSuppressors({ PhasedPipelineChecker(it) })
        useFailureSuppressors(::UpdateTestDataHandler)

        // Compares the expected `<!DIAGNOSTIC_ID!>` source markers (id + range) with the
        // diagnostics actually reported by the frontend.
        enableMetaInfoHandler()

        configureFirHandlersStep {
            useHandlers(::FirDiagnosticsHandler)
        }

        configureJvmArtifactsHandlersStep {
            // JVM-signature-level diagnostics (e.g. CONFLICTING_JVM_DECLARATIONS) are
            // reported during the backend phase and asserted against the same markers.
            useHandlers(::JvmBackendDiagnosticsHandler)
        }
    }
}
