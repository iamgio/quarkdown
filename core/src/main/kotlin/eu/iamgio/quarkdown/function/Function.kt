package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.OutputValueType

/**
 *
 */
interface Function<T, VT : OutputValueType<T>> {
    val name: String
    val parameters: List<FunctionParameter<*>>
    val invoke: (List<FunctionCallArgument<*>>) -> T
}
