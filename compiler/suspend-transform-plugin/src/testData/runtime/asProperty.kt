// FULL_JDK
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class Config {
    @JvmBlocking(asProperty = true)
    suspend fun mode(): String = "mode:prod"
}

fun box(): String {
    val config = Config()

    val getter = Config::class.java.getMethod("getModeBlocking")
    val dispatched = getter.invoke(config) as String
    if (dispatched != "mode:prod") return "FAIL: property getter dispatched to '$dispatched'"

    val functionMembers = Config::class.java.declaredMethods.map { it.name }.filter { it == "modeBlocking" }
    if (functionMembers.isNotEmpty()) return "FAIL: asProperty generated a function member: $functionMembers"

    return "OK"
}
