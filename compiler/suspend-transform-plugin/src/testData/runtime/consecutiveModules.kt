// FULL_JDK
// MODULE: first
// FILE: first.kt
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class FirstService {
    @JvmBlocking
    suspend fun alpha(): String = "alpha"
}

// MODULE: second(first)
// FILE: second.kt
import love.forte.plugin.suspendtrans.annotation.JvmBlocking

class SecondService {
    @JvmBlocking
    suspend fun beta(): String = "beta"
}

fun box(): String {
    val secondMembers = SecondService::class.java.methods.map { it.name }.toSet()
    if ("betaBlocking" !in secondMembers) return "FAIL: betaBlocking missing from $secondMembers"
    // The first consecutive compilation must not leak its generated members into the second one.
    if ("alphaBlocking" in secondMembers) return "FAIL: stale generated member alphaBlocking leaked into $secondMembers"

    val firstMembers = FirstService::class.java.methods.map { it.name }.toSet()
    if ("alphaBlocking" !in firstMembers) return "FAIL: alphaBlocking missing from $firstMembers"
    if ("betaBlocking" in firstMembers) return "FAIL: stale generated member betaBlocking leaked into $firstMembers"

    return "OK"
}
