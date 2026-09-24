// RUN_PIPELINE_TILL: FRONTEND
// LATEST_PHASE_IN_PIPELINE: FRONTEND
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class TokenService {
    @JvmBlocking
    suspend fun token(): String = "token"
}

fun probe(service: TokenService) {
    service.<!UNRESOLVED_REFERENCE!>tokenAsync<!>()
}
