package eu.iamgio.quarkdown.function

import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.call.binding.ArgumentBindings
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
     * The [ArgumentBindings] allow looking up argument values by their parameter.
     */
    val invoke: (ArgumentBindings) -> T
}

/**
 * A basic [Function] implementation.
 * @see Function
 */
data class SimpleFunction<T : OutputValue<*>>(
    override val name: String,
    override val parameters: List<FunctionParameter<*>>,
    override val invoke: (ArgumentBindings) -> T,
) : Function<T>

fun Function<*>.signatureAsString(includeName: Boolean = true) =
    buildString {
        if (includeName) {
            append(name)
        }
        append("(")
        append(
            parameters.joinToString { parameter ->
                buildString {
                    if (parameter.isOptional) append("optional ")
                    parameter.type.simpleName?.let { append(it).append(" ") }
                    append(parameter.name)
                }
            },
        )
        append(")")
    }
