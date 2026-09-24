package love.forte.plugin.suspendtrans.services

import love.forte.plugin.suspendtrans.SuspendTransformComponentRegistrar
import love.forte.plugin.suspendtrans.configuration.*
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jsPromiseTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmAsyncTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmBlockingTransformer
import love.forte.plugin.suspendtrans.configuration.SuspendTransformConfigurations.jvmReactiveTransformer
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.configureJdkClasspathRoots
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.EnvironmentConfigurator
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

/**
 * Inject SuspendTransform plugin into test environment
 */
class SuspendTransformerEnvironmentConfigurator(testServices: TestServices) : EnvironmentConfigurator(testServices) {

    @OptIn(ExperimentalCompilerApi::class, InternalSuspendTransformConfigurationApi::class)
    override fun CompilerPluginRegistrar.ExtensionStorage.registerCompilerExtensions(
        module: TestModule,
        configuration: CompilerConfiguration
    ) {
        val testConfiguration = love.forte.plugin.suspendtrans.configuration.SuspendTransformConfiguration(
            transformers = mapOf(
                TargetPlatform.JS to listOf(jsPromiseTransformer),
                TargetPlatform.JVM to listOf(
                    jvmBlockingTransformer,
                    jvmAsyncTransformer,
                    jvmReactiveTransformer,
                    nullmarkModeTransformer("AsyncNullable", "NullableAsync", TransformReturnTypeGenericMode.NULLABLE),
                    nullmarkModeTransformer("AsyncNonNull", "NonNullAsync", TransformReturnTypeGenericMode.NON_NULL),
                )
            )
        )
        // register plugin
        SuspendTransformComponentRegistrar.register(this, testConfiguration)
    }

    override fun configureCompilerConfiguration(configuration: CompilerConfiguration, module: TestModule) {
        configuration.put(JVMConfigurationKeys.NO_JDK, false)
        configuration.configureJdkClasspathRoots()

        // register runtimes
        for (runtimeJarFile in resolveSuspendTransformRuntimeJars()) {
            configuration.addJvmClasspathRoot(runtimeJarFile)
        }
    }

}

/**
 * Class names whose containing jars must be present when compiling or running
 * suspend-transform test samples.
 */
private val SUSPEND_TRANSFORM_RUNTIME_CLASS_NAMES: List<String> = listOf(
    // runtimes
    "love.forte.plugin.suspendtrans.runtime.RunInSuspendJvmKt",
    "love.forte.plugin.suspendtrans.annotation.JvmAsync",
    "love.forte.plugin.suspendtrans.annotation.JvmBlocking",
    "love.forte.plugin.suspendtrans.annotation.JvmReactive",

    // coroutines
    "kotlinx.coroutines.CoroutineScope",
    "kotlinx.coroutines.reactive.PublishKt",
    "org.reactivestreams.Publisher",
)

/**
 * Resolves the jars that have to be on the classpath of test compilations and of
 * reflectively executed test samples. Shared by the compile-time environment
 * configurator and the runtime classpath provider used by box-style oracle tests,
 * so both sides always see the same runtime.
 */
internal fun resolveSuspendTransformRuntimeJars(): List<File> =
    SUSPEND_TRANSFORM_RUNTIME_CLASS_NAMES.mapNotNullTo(mutableListOf()) { className ->
        try {
            PathUtil.getResourcePathForClass(Class.forName(className))
        } catch (_: ClassNotFoundException) {
            System.err.println("Runtime jar '$className' not found!")
            null
        }
    }

@OptIn(InternalSuspendTransformConfigurationApi::class)
private fun nullmarkModeTransformer(
    annotationName: String,
    defaultSuffix: String,
    mode: TransformReturnTypeGenericMode,
): Transformer =
    Transformer(
        markAnnotation = MarkAnnotation(
            classInfo = ClassInfo("", annotationName),
            defaultSuffix = defaultSuffix,
            markNameProperty = null,
        ),
        transformFunctionInfo = jvmAsyncTransformer.transformFunctionInfo,
        transformReturnType = jvmAsyncTransformer.transformReturnType,
        transformReturnTypeGeneric = true,
        originFunctionIncludeAnnotations = jvmAsyncTransformer.originFunctionIncludeAnnotations,
        syntheticFunctionIncludeAnnotations = listOf(
            IncludeAnnotation(ClassInfo("love.forte.plugin.suspendtrans.annotation", "Api4J"), includeProperty = true)
        ),
        copyAnnotationsToSyntheticFunction = false,
        copyAnnotationExcludes = emptyList(),
        transformReturnTypeGenericMode = mode,
    )
