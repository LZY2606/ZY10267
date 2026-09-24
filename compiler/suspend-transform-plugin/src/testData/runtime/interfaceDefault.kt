// FULL_JDK
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking
import java.util.concurrent.CompletableFuture

interface Greeter {
    @JvmBlocking
    @JvmAsync
    suspend fun greet(): String = "greet:Greeter"
}

interface Named {
    @JvmBlocking
    suspend fun name(): String = "name:Named"
}

class GreeterImpl : Greeter, Named

fun box(): String {
    val impl = GreeterImpl()

    val greetBlocking = GreeterImpl::class.java.getMethod("greetBlocking")
    val greetDispatched = greetBlocking.invoke(impl) as String
    if (greetDispatched != "greet:Greeter") return "FAIL: greetBlocking dispatched to '$greetDispatched'"

    val nameBlocking = GreeterImpl::class.java.getMethod("nameBlocking")
    val nameDispatched = nameBlocking.invoke(impl) as String
    if (nameDispatched != "name:Named") return "FAIL: nameBlocking dispatched to '$nameDispatched'"

    val greetAsync = GreeterImpl::class.java.getMethod("greetAsync")
    val asyncDispatched = (greetAsync.invoke(impl) as CompletableFuture<*>).get() as String
    if (asyncDispatched != "greet:Greeter") return "FAIL: greetAsync dispatched to '$asyncDispatched'"

    return "OK"
}
