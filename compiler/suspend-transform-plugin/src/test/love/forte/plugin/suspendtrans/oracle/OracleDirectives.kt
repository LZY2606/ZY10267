package love.forte.plugin.suspendtrans.oracle

import org.jetbrains.kotlin.test.directives.model.DirectiveApplicability
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

/**
 * Directives consumed by the three-layer oracle handlers
 * (source diagnostic / ABI / runtime).
 */
object OracleDirectives : SimpleDirectivesContainer() {
    /**
     * `// ORACLE_DIAGNOSTIC: FACTORY_NAME@line:col-line:col, ...`
     *
     * Expected ERROR diagnostics as diagnostic id + source range only.
     * When absent, no ERROR diagnostics are expected at all.
     */
    val ORACLE_DIAGNOSTIC by stringDirective(
        "Expected ERROR diagnostics as FACTORY_NAME@line:col-line:col entries",
        DirectiveApplicability.Global,
        multiLine = true,
    )

    /**
     * `// ORACLE_ABI: some.ClassName, other.ClassName`
     *
     * Classes whose normalized public member signatures are compared
     * against `<test>.abi.txt`.
     */
    val ORACLE_ABI by stringDirective(
        "Classes whose normalized public ABI is compared with <test>.abi.txt",
        DirectiveApplicability.Global,
        multiLine = true,
    )

    /**
     * `// ORACLE_CALL: some.Class#method, other.Class#getX('a', 1, null, true)`
     *
     * Public members invoked reflectively; the dispatched result is compared
     * against `<test>.run.txt`.
     */
    val ORACLE_CALL by stringDirective(
        "Members invoked reflectively; results are compared with <test>.run.txt",
        DirectiveApplicability.Global,
        multiLine = true,
    )
}
