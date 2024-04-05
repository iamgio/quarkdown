package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.OutputValue

/**
 * @param T type of the
 */
data class FunctionCall<T : OutputValue<*>>(
    val function: Function<T>,
    val arguments: List<FunctionCallArgument<*>>,
) {
    fun execute(): T = function.invoke(arguments)
}
