// ORACLE_DIAGNOSTIC: CONFLICTING_JVM_DECLARATIONS@4:1-9:2, CONFLICTING_JVM_DECLARATIONS@8:5-8:29
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class Config {
    @JvmBlocking(asProperty = true)
    suspend fun name(): String = "name"

    val nameBlocking: String = "existing"
}
