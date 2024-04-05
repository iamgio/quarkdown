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
    // Allows linking arguments to their parameter.
    private val linker = FunctionArgumentsLinker(this)

    init {
        linker.link()
    }

    /**
     * Whether this is a valid function call, meaning arguments match parameter count and types.
     */
    val isValid: Boolean
        get() = linker.isCompliant

    /**
     * Checks the call validity and calls the function.
     * @return the function output
     */
    fun execute(): T {
        if (!isValid) {
            // TODO error
        }
        return function.invoke(linker)
    }
}
