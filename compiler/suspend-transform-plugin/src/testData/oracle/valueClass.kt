// FULL_JDK
// JVM_TARGET: 21
// ORACLE_ABI: TokenBox
// ORACLE_CALL: TokenBox#tokenBlocking-BtjUYRY, TokenBox#tokenAsync
import love.forte.plugin.suspendtrans.annotation.JvmAsync
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

@JvmInline
value class Token(val raw: String)

class TokenBox {
    @JvmBlocking
    @JvmAsync
    suspend fun token(): Token = Token("token-42")
}
