// ALLOW_DANGEROUS_LANGUAGE_VERSION_TESTING
// LANGUAGE_VERSION: 2.2
// API_VERSION: 2.2
// JVM_TARGET: 1.8
// ORACLE_ABI: Config
// ORACLE_CALL: Config#getNameBlocking, Config#getNameAsync
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class Config {
    @JvmBlocking(asProperty = true)
    @JvmAsync(asProperty = true)
    suspend fun name(): String = "config-name"
}
