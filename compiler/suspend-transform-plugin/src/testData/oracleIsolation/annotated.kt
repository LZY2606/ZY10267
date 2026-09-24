// ORACLE_ABI: StaleProbe
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class StaleProbe {
    @JvmBlocking
    suspend fun foo(): String = "foo"
}
