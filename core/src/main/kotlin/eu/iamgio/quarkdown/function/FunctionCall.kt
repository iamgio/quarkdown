package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.OutputValueType

/**
 * @param T type of the
 */
data class FunctionCall<T, VT : OutputValueType<T>>(
    val function: Function<T, VT>,
    val arguments: List<FunctionCallArgument<*>>,
) {
    fun execute(): T = function.invoke(arguments)
}
