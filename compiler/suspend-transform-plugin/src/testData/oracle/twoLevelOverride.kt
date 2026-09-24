// ALLOW_DANGEROUS_LANGUAGE_VERSION_TESTING
// LANGUAGE_VERSION: 2.1
// API_VERSION: 2.1
// JVM_TARGET: 11
// ORACLE_ABI: Mid, Derived
// ORACLE_CALL: Mid#tokenBlocking, Derived#tokenBlocking, Derived#tokenAsync
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

interface TokenSource {
    @JvmBlocking
    @JvmAsync
    suspend fun token(): String = "TokenSource.base"
}

open class Mid : TokenSource {
    override suspend fun token(): String = "Mid.mid"
}

class Derived : Mid() {
    override suspend fun token(): String = "Derived.derived"
}
