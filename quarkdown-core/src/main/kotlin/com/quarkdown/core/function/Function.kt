package com.quarkdown.core.function

import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.call.binding.ArgumentBindings
import com.quarkdown.core.function.call.validate.FunctionCallValidator
import com.quarkdown.core.function.value.OutputValue

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
    val parameters: List<FunctionParameter>

    /**
     * Validators that check the validity of a function call towards this function.
     * If a condition is not met during the validation, an exception should be thrown.
     */
    val validators: List<FunctionCallValidator<T>>

    /**
     * Function that maps the input arguments into an output value.
     * Arguments and [parameters] compliance in terms of matching types and count is not checked here.
     * The [ArgumentBindings] allow looking up argument values by their parameter.
     *
     * - [ArgumentBindings]: bindings between parameters and arguments for the function call
     * - [FunctionCall]: the function call that triggered this invocation
     */
    val invoke: (ArgumentBindings, FunctionCall<T>) -> T
}

/**
 * A basic [Function] implementation.
 * @see Function
 */
data class SimpleFunction<T : OutputValue<*>>(
    override val name: String,
    override val parameters: List<FunctionParameter>,
    override val validators: List<FunctionCallValidator<T>> = emptyList(),
    override val invoke: (ArgumentBindings, FunctionCall<T>) -> T,
) : Function<T>

/**
 * Renders this function's signature as a human-readable string.
 * Injected parameters ([FunctionParameter.isInjected]) are omitted — they aren't part of the
 * surface that callers see and shouldn't clutter the rendered signature.
 * @param includeName whether the function name should precede the parameter list. Callers that
 *                    embed the signature in a sentence typically pass `false` so the sentence
 *                    provides its own subject.
 * @return the rendered signature, e.g. `foo(String name, optional Int count)`, or
 *         `(String name)` when [includeName] is `false`.
 */
fun Function<*>.signatureAsString(includeName: Boolean = true) =
    buildString {
        if (includeName) {
            append(name)
        }
        append("(")
        append(
            parameters
                .filterNot { it.isInjected }
                .joinToString { parameter ->
                    buildString {
                        if (parameter.isOptional) append("optional ")
                        append(parameter.type.displayName).append(" ")
                        append(parameter.name)
                    }
                },
        )
        append(")")
    }
