package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.InputValue

/**
 *
 */
data class FunctionCallArgument<T>(
    val value: InputValue<T>,
    val parameter: FunctionParameter<T>,
)
