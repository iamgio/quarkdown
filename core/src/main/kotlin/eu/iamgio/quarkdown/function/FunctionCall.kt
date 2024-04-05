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
     * Whether this is a valid function call, meaning arguments match parameter count and types.
     */
    val isValid: Boolean
        get() {
            // TODO check arguments amount and types
            return true
        }

    /**
     *
     */
    fun execute(): T {
        if (!isValid) {
            // TODO error
        }
        return function.invoke(arguments)
    }
}
