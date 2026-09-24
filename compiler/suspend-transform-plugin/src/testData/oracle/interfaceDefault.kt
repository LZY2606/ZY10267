// ALLOW_DANGEROUS_LANGUAGE_VERSION_TESTING
// LANGUAGE_VERSION: 2.0
// API_VERSION: 2.0
// JVM_TARGET: 1.8
// ORACLE_ABI: Greeting, EnglishGreeter, CustomGreeter
// ORACLE_CALL: EnglishGreeter#greetBlocking, CustomGreeter#greetBlocking, EnglishGreeter#greetAsync, CustomGreeter#greetAsync
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

interface Greeting {
    @JvmBlocking
    @JvmAsync
    suspend fun greet(): String = "Greeting.default"
}

class EnglishGreeter : Greeting

class CustomGreeter : Greeting {
    override suspend fun greet(): String = "CustomGreeter.override"
}
