// ALLOW_DANGEROUS_LANGUAGE_VERSION_TESTING
// LANGUAGE_VERSION: 2.0
// API_VERSION: 2.0
// JVM_TARGET: 17
// ORACLE_ABI: ScopeRunner
// ORACLE_CALL: ScopeRunner#pingBlocking, ScopeRunner#pingAsync
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class ScopeRunner : CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Unconfined

    @JvmBlocking
    @JvmAsync
    suspend fun ping(): String = "pong"
}
