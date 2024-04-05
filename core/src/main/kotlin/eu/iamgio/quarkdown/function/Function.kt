package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.OutputValue

/**
 *
 */
interface Function<T : OutputValue<*>> {
    val name: String
    val parameters: List<FunctionParameter<*>>
    val invoke: (List<FunctionCallArgument<*>>) -> T
}
