// FULL_JDK
// ALLOW_DANGEROUS_LANGUAGE_VERSION_TESTING
// LANGUAGE_VERSION: 2.4
// API_VERSION: 2.4
// JVM_TARGET: 21
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

class AxisScopedService(override val coroutineContext: CoroutineContext) : CoroutineScope {
    @JvmAsync
    suspend fun scopeName(): String = kotlin.coroutines.coroutineContext[CoroutineName]?.name ?: "<none>"
}

fun box(): String {
    val service = AxisScopedService(CoroutineName("axis-24") + Job())
    val bridge = AxisScopedService::class.java.getMethod("scopeNameAsync")
    val dispatched = (bridge.invoke(service) as CompletableFuture<*>).get() as String
    if (dispatched != "axis-24") return "FAIL: [LV=2.4, JVM_TARGET=21] scopeNameAsync dispatched in scope '$dispatched'"
    return "OK"
}
