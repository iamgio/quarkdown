package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.InputValueType

/**
 *
 */
data class FunctionParameter<T>(
    val name: String,
    val type: InputValueType<T>,
)
