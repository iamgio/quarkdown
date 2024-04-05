package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.InputValue

/**
 *
 */
data class FunctionParameter<T>(
    val name: String,
    val type: InputValue<T>,
)
