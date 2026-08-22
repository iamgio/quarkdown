package com.quarkdown.processor.coercion

/**
 * How the generator fills one parameter of a generated dynamic function body.
 */
sealed interface CoercionPlan {
    /**
     * A parameter converted from a call argument via `ValueFactory`.
     *
     * @param parameterTypeExpression Kotlin source of the `ParameterType` to store in the descriptor
     * @param factoryExpression body of the coercion lambda, with `it` bound to the raw value and
     *                          `call` bound to the enclosing function call
     */
    data class ViaFactory(
        val parameterTypeExpression: String,
        val factoryExpression: String,
    ) : CoercionPlan

    /**
     * A parameter supplied from the function call rather than the call site.
     *
     * @param parameterTypeExpression Kotlin source of the `ParameterType` to store in the descriptor
     * @param localExpression Kotlin source that initializes the parameter's local
     */
    data class ViaInjection(
        val parameterTypeExpression: String,
        val localExpression: String,
    ) : CoercionPlan

    /**
     * No plan could be made. The processor turns this into a KSP error.
     * @param reason message shown to the library author
     */
    data class Unsupported(
        val reason: String,
    ) : CoercionPlan
}
