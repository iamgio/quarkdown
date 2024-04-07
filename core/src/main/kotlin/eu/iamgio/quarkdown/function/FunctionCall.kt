package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.OutputValue

/**
 * A call to a declared [Function].
 * @param function referenced function to call
 * @param arguments arguments of the call
 * @param T expected output type of the function
 */
data class FunctionCall<T : OutputValue<*>>(
    val function: Function<T>,
    val arguments: List<FunctionCallArgument<*>>,
) {
    /**
     * Checks the call validity and calls the function.
     * @return the function output
     */
    fun execute(): T {
        // Allows linking arguments to their parameter.
        val linker = FunctionArgumentsLinker(this)

        linker.link()
        return function.invoke(linker)
    }
}
