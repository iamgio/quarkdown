package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.InputValue

/**
 * An argument of a [FunctionCall].
 * @param value value of the argument
 * @param parameter referenced corresponding function parameter
 * @param T input type of the argument
 */
data class FunctionCallArgument<T>(
    val value: InputValue<T>,
    val parameter: FunctionParameter<T>,
)
