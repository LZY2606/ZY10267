// ORACLE_DIAGNOSTIC: CONFLICTING_JVM_DECLARATIONS@6:13-6:38, CONFLICTING_JVM_DECLARATIONS@8:5-8:41
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class Conflict {
    @JvmBlocking
    suspend fun foo(): String = "foo"

    fun fooBlocking(): String = "manual"
}
