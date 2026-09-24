// FULL_JDK
// ALLOW_DANGEROUS_LANGUAGE_VERSION_TESTING
// LANGUAGE_VERSION: 2.2
// API_VERSION: 2.2
// JVM_TARGET: 17
// ORACLE_ABI: Box, Shouter
// ORACLE_CALL: Box#sizeBlocking, Shouter#shoutBlocking(null), Shouter#shoutBlocking('hey')
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class Box<T>(val value: T?) {
    constructor() : this(null)

    @JvmBlocking
    suspend fun size(): Int = value?.toString()?.length ?: -1
}

class Shouter {
    @JvmBlocking
    suspend fun String?.shout(): String = this?.uppercase() ?: "null-receiver"
}
