// FULL_JDK
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

open class BaseHandler {
    @JvmBlocking
    open suspend fun handle(): String = "Base"
}

class DerivedHandler : BaseHandler() {
    @JvmBlocking
    override suspend fun handle(): String = "Derived"
}

fun box(): String {
    val baseBridge = BaseHandler::class.java.getMethod("handleBlocking")
    val derivedBridge = DerivedHandler::class.java.getMethod("handleBlocking")

    // Correct receivers: record which implementation actually runs.
    val onBase = baseBridge.invoke(BaseHandler()) as String
    if (onBase != "Base") return "FAIL: base bridge dispatched to '$onBase'"
    val onDerived = derivedBridge.invoke(DerivedHandler()) as String
    if (onDerived != "Derived") return "FAIL: derived bridge dispatched to '$onDerived'"

    // Virtual dispatch follows the actual receiver, not the bridge owner.
    val viaBaseOnDerived = baseBridge.invoke(DerivedHandler()) as String
    if (viaBaseOnDerived != "Derived") return "FAIL: base bridge on derived receiver dispatched to '$viaBaseOnDerived'"

    // Deliberately wrong dispatch receiver: DerivedHandler's bridge invoked with a BaseHandler
    // instance. The source compiles fine; only the runtime behavior can catch the mistake.
    val wrongReceiverCaught = try {
        derivedBridge.invoke(BaseHandler())
        false
    } catch (e: IllegalArgumentException) {
        true
    }
    if (!wrongReceiverCaught) return "FAIL: wrong dispatch receiver was not caught at runtime"

    return "OK"
}
