package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.value.OutputValue

/**
 * A function that can be called from a Quarkdown source via a [FunctionCall].
 * @param T expected output type
 */
interface Function<T : OutputValue<*>> {
    /**
     * Function name.
     */
    val name: String

    /**
     * Declared parameters.
     */
    val parameters: List<FunctionParameter<*>>

    /**
     * Function that maps the input arguments into an output value.
     * Arguments and [parameters] compliance in terms of matching types and count is not checked here.
     * The [FunctionArgumentsLinker] allows looking up argument values by their parameter name.
     */
    val invoke: FunctionArgumentsLinker.() -> T
}

/**
 * A basic [Function] implementation.
 * @see Function
 */
data class SimpleFunction<T : OutputValue<*>>(
    override val name: String,
    override val parameters: List<FunctionParameter<*>>,
    override val invoke: FunctionArgumentsLinker.() -> T,
) : Function<T>
