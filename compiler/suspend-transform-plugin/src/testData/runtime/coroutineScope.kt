// FULL_JDK
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.CoroutineContext

class ScopedService(override val coroutineContext: CoroutineContext) : CoroutineScope {
    @JvmAsync
    suspend fun scopeName(): String = kotlin.coroutines.coroutineContext[CoroutineName]?.name ?: "<none>"
}

fun box(): String {
    val service = ScopedService(CoroutineName("oracle-scope") + Job())

    val scopeNameAsync = ScopedService::class.java.getMethod("scopeNameAsync")
    val dispatched = (scopeNameAsync.invoke(service) as CompletableFuture<*>).get() as String
    if (dispatched != "oracle-scope") return "FAIL: async bridge dispatched in scope '$dispatched'"

    return "OK"
}
