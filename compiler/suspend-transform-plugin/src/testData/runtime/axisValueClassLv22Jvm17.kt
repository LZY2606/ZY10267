// FULL_JDK
// ALLOW_DANGEROUS_LANGUAGE_VERSION_TESTING
// LANGUAGE_VERSION: 2.2
// API_VERSION: 2.2
// JVM_TARGET: 17
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

@JvmInline
value class AxisToken(val raw: String)

class AxisTokenSource {
    @JvmBlocking
    suspend fun token(): AxisToken = AxisToken("axis-22")
}

fun box(): String {
    val bridge = AxisTokenSource::class.java.declaredMethods.single { it.name.startsWith("tokenBlocking") }
    val result = bridge.invoke(AxisTokenSource())
    if (result != "axis-22") return "FAIL: [LV=2.2, JVM_TARGET=17] tokenBlocking returned $result"
    return "OK"
}
