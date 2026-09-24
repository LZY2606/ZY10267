// FULL_JDK
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

open class BaseWorker {
    @JvmBlocking
    @JvmAsync
    open suspend fun work(): String = "Base"
}

open class MidWorker : BaseWorker() {
    @JvmBlocking
    @JvmAsync
    override suspend fun work(): String = "Mid"
}

class LeafWorker : MidWorker() {
    @JvmBlocking
    @JvmAsync
    override suspend fun work(): String = "Leaf"
}

fun box(): String {
    val dispatchLog = sortedMapOf<String, String>()

    fun record(bridgeOwner: Class<*>, receiver: Any) {
        val bridge = bridgeOwner.getMethod("workBlocking")
        dispatchLog["${bridgeOwner.simpleName}->${receiver.javaClass.simpleName}"] = bridge.invoke(receiver) as String
    }

    record(BaseWorker::class.java, BaseWorker())
    record(BaseWorker::class.java, MidWorker())
    record(BaseWorker::class.java, LeafWorker())
    record(MidWorker::class.java, MidWorker())
    record(MidWorker::class.java, LeafWorker())
    record(LeafWorker::class.java, LeafWorker())

    val expected = mapOf(
        "BaseWorker->BaseWorker" to "Base",
        "BaseWorker->MidWorker" to "Mid",
        "BaseWorker->LeafWorker" to "Leaf",
        "MidWorker->MidWorker" to "Mid",
        "MidWorker->LeafWorker" to "Leaf",
        "LeafWorker->LeafWorker" to "Leaf",
    )

    if (dispatchLog != expected.toSortedMap()) return "FAIL: dispatch log $dispatchLog"

    return "OK"
}
