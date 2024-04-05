package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.InputValueType
import eu.iamgio.quarkdown.function.value.Value

/**
 *
 */
data class FunctionCallArgument<T>(
    val value: Value<T, InputValueType<T>>,
    val parameter: FunctionParameter<T>,
)
