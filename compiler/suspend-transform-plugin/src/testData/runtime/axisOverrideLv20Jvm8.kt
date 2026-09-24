// FULL_JDK
// ALLOW_DANGEROUS_LANGUAGE_VERSION_TESTING
// LANGUAGE_VERSION: 2.0
// API_VERSION: 2.0
// JVM_TARGET: 1.8
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

open class AxisBase {
    @JvmBlocking
    open suspend fun ping(): String = "base"
}

class AxisLeaf : AxisBase() {
    @JvmBlocking
    override suspend fun ping(): String = "leaf"
}

fun box(): String {
    val bridge = AxisLeaf::class.java.getMethod("pingBlocking")
    val dispatched = bridge.invoke(AxisLeaf()) as String
    if (dispatched != "leaf") return "FAIL: [LV=2.0, JVM_TARGET=1.8] pingBlocking dispatched to '$dispatched'"
    return "OK"
}
