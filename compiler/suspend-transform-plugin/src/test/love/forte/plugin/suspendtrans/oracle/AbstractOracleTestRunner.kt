package love.forte.plugin.suspendtrans.oracle

import love.forte.plugin.suspendtrans.services.SuspendTransformerEnvironmentConfigurator
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureJvmArtifactsHandlersStep
import org.jetbrains.kotlin.test.configuration.setupJvmPipelineStepsWithoutCompilationErrorHandlers
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_PARSER
import org.jetbrains.kotlin.test.initIdeaConfiguration
import org.jetbrains.kotlin.test.model.ArtifactKinds
import org.jetbrains.kotlin.test.model.DependencyKind
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.junit.jupiter.api.BeforeAll

/**
 * Runner for the three-layer oracle suite: source diagnostics
 * ([OracleDiagnosticHandler]), ABI ([OracleAbiHandler]) and runtime dispatch
 * ([OracleRuntimeHandler]).
 */
abstract class AbstractOracleTestRunner : AbstractKotlinCompilerTest() {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            initIdeaConfiguration()
        }
    }

    override fun configure(builder: TestConfigurationBuilder) {
        // The pipeline without compilation-error handlers: the diagnostic
        // oracle takes over the role of NoFir/NoJvmCompilationErrorsHandler so
        // that failure fixtures can pin diagnostic ids and source ranges.
        builder.setupJvmPipelineStepsWithoutCompilationErrorHandlers(FirParser.LightTree)

        builder.useDirectives(OracleDirectives)

        builder.globalDefaults {
            targetBackend = TargetBackend.JVM_IR
            targetPlatform = JvmPlatforms.defaultJvmPlatform
            artifactKind = ArtifactKinds.Jvm
            dependencyKind = DependencyKind.Binary
        }

        builder.defaultDirectives {
            FIR_PARSER with FirParser.LightTree
        }

        builder.configureJvmArtifactsHandlersStep {
            useHandlers(::OracleDiagnosticHandler, ::OracleAbiHandler, ::OracleRuntimeHandler)
        }

        builder.useConfigurators(
            ::SuspendTransformerEnvironmentConfigurator,
        )
    }

    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return EnvironmentBasedStandardLibrariesPathProvider
    }
}
