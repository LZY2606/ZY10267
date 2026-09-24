// FULL_JDK
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class EchoBox<T> {
    @JvmBlocking
    suspend fun T?.echoOrNull(): String = if (this == null) "null" else "value:$this"
}

fun box(): String {
    val box0 = EchoBox<String>()
    val bridge = EchoBox::class.java.getMethod("echoOrNullBlocking", Any::class.java)

    val onNull = bridge.invoke(box0, null) as String
    if (onNull != "null") return "FAIL: nullable receiver dispatched to '$onNull'"

    val onValue = bridge.invoke(box0, "echo") as String
    if (onValue != "value:echo") return "FAIL: generic receiver dispatched to '$onValue'"

    return "OK"
}
