import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class TokenService {
    @JvmBlocking
    suspend <!CONFLICTING_JVM_DECLARATIONS!>fun token(): String = "token"<!>

    <!CONFLICTING_JVM_DECLARATIONS!>fun tokenBlocking(): String = "manual"<!>
}
