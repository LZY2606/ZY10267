// ORACLE_CALL: Derived#tokenBlocking
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

interface TokenSource {
    @JvmBlocking
    suspend fun token(): String = "TokenSource.base"
}

open class Mid : TokenSource {
    override suspend fun token(): String = "Mid.mid"
}

class Derived : Mid() {
    override suspend fun token(): String = "Derived.derived"
}
