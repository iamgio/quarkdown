package com.quarkdown.core.function

/**
 * The kind of value a [FunctionParameter] accepts.
 *
 * For native functions this is decided at build time by the native library processor, which emits
 * the matching variant into the generated module.
 */
sealed interface ParameterType {
    /**
     * Name shown in rendered signatures and in argument mismatch errors, e.g. `Size`.
     */
    val displayName: String

    /**
     * A parameter whose conversion from a loosely typed argument is generated at build time.
     * @param displayName simple name of the accepted static type
     */
    data class Static(
        override val displayName: String,
    ) : ParameterType

    /**
     * A parameter that receives a [com.quarkdown.core.function.value.DynamicValue] verbatim,
     * without conversion. Used by user-defined functions, whose arguments stay raw Quarkdown text.
     */
    data object Dynamic : ParameterType {
        override val displayName: String
            get() = "Dynamic"
    }

    /**
     * A parameter that accepts a lambda block. Enables the binder's fast path that parses an
     * inline argument as a lambda instead of evaluating it as an expression first.
     */
    data object LambdaBlock : ParameterType {
        override val displayName: String
            get() = "Lambda"
    }

    /**
     * A parameter supplied from the [com.quarkdown.core.function.call.FunctionCall] rather than by the call site.
     * @param kind which piece of the call to supply
     */
    data class Injected(
        val kind: InjectionKind,
    ) : ParameterType {
        override val displayName: String
            get() = kind.displayName
    }
}

/**
 * What an [ParameterType.Injected] parameter receives.
 * @param displayName name shown in rendered signatures
 */
enum class InjectionKind(
    val displayName: String,
) {
    /** The context the call lies in. */
    CONTEXT("Context"),

    /** The call itself. */
    CALL("FunctionCall"),

    /** The AST node that produced the call. */
    SOURCE_NODE("FunctionCallNode"),
}
