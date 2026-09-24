// RUN_PIPELINE_TILL: FRONTEND
// LATEST_PHASE_IN_PIPELINE: FRONTEND
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class TokenService {
    @JvmBlocking
    suspend fun token(id: Int): String = "token"
}

fun probe(service: TokenService) {
    service.<!OPT_IN_USAGE!>tokenBlocking<!>(<!ARGUMENT_TYPE_MISMATCH!>"not-an-int"<!>)
    service.<!NO_VALUE_FOR_PARAMETER, OPT_IN_USAGE!>tokenBlocking<!>()
}
