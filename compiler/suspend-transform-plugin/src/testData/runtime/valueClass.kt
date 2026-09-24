// FULL_JDK
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import java.util.concurrent.CompletableFuture

@JvmInline
value class UserId(val raw: String)

class IdRepository {
    @JvmBlocking
    @JvmAsync
    suspend fun nextId(): UserId = UserId("id-42")
}

fun box(): String {
    val repository = IdRepository()

    // The blocking bridge of a value-class-returning suspend function is name-mangled
    // and returns the unboxed carrier type.
    val blocking = IdRepository::class.java.declaredMethods.single { it.name.startsWith("nextIdBlocking") }
    val blockingResult = blocking.invoke(repository)
    if (blockingResult != "id-42") return "FAIL: blocking bridge returned $blockingResult"

    val async = IdRepository::class.java.getMethod("nextIdAsync")
    val asyncResult = (async.invoke(repository) as CompletableFuture<*>).get()
    if (asyncResult !is UserId || asyncResult.raw != "id-42") return "FAIL: async bridge returned $asyncResult"

    return "OK"
}
