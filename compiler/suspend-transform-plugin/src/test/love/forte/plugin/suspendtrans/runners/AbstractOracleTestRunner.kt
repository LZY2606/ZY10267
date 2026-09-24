package love.forte.plugin.suspendtrans.runners

import love.forte.plugin.suspendtrans.services.SuspendTransformerEnvironmentConfigurator
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.configuration.setupJvmPipelineSteps
import org.jetbrains.kotlin.test.configuration.setupJvmPipelineStepsWithoutCompilationErrorHandlers
import org.jetbrains.kotlin.test.directives.FirDiagnosticsDirectives.FIR_PARSER
import org.jetbrains.kotlin.test.initIdeaConfiguration
import org.jetbrains.kotlin.test.model.ArtifactKinds
import org.jetbrains.kotlin.test.model.DependencyKind
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.services.EnvironmentBasedStandardLibrariesPathProvider
import org.jetbrains.kotlin.test.services.KotlinStandardLibrariesPathProvider
import org.junit.jupiter.api.BeforeAll

/**
 * Shared base for the three-layer oracle runners (source diagnostics, ABI, runtime).
 *
 * Each test case is compiled by a fresh compiler environment provided by the test
 * framework, so no compile daemon or cache state is shared between cases.
 */
abstract class AbstractOracleTestRunner : AbstractKotlinCompilerTest() {
    companion object {
        @BeforeAll
        @JvmStatic
        fun setUp() {
            initIdeaConfiguration() // set system property to initialize idea service
        }
    }

    override fun configure(builder: TestConfigurationBuilder) {
        builder.setupPipelineSteps()

        builder.globalDefaults {
            targetBackend = TargetBackend.JVM_IR
            targetPlatform = JvmPlatforms.defaultJvmPlatform
            artifactKind = ArtifactKinds.Jvm
            dependencyKind = DependencyKind.Binary
        }

        builder.defaultDirectives {
            FIR_PARSER with FirParser.LightTree
        }

        builder.configureOracleHandlers()

        builder.useConfigurators(
            ::SuspendTransformerEnvironmentConfigurator,    // compiler plugin configuration
        )
    }

    protected open fun TestConfigurationBuilder.setupPipelineSteps() {
        setupJvmPipelineSteps(FirParser.LightTree)
    }

    protected fun TestConfigurationBuilder.setupPipelineStepsWithoutCompilationErrorHandlers() {
        setupJvmPipelineStepsWithoutCompilationErrorHandlers(FirParser.LightTree)
    }

    abstract fun TestConfigurationBuilder.configureOracleHandlers()

    override fun createKotlinStandardLibrariesPathProvider(): KotlinStandardLibrariesPathProvider {
        return EnvironmentBasedStandardLibrariesPathProvider
    }
}
