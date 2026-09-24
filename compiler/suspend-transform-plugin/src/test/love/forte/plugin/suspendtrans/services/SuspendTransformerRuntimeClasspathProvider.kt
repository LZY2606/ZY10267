package love.forte.plugin.suspendtrans.services

import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.RuntimeClasspathProvider
import org.jetbrains.kotlin.test.services.TestServices
import java.io.File

/**
 * Provides the suspend-transform runtime and coroutines jars to class loaders that
 * execute compiled test samples, mirroring the compile-time classpath assembled by
 * [SuspendTransformerEnvironmentConfigurator].
 */
class SuspendTransformerRuntimeClasspathProvider(testServices: TestServices) : RuntimeClasspathProvider(testServices) {
    override fun runtimeClassPaths(module: TestModule): List<File> = resolveSuspendTransformRuntimeJars()
}
