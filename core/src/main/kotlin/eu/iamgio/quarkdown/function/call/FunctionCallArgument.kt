package eu.iamgio.quarkdown.function.call

import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.value.InputValue

/**
 * An argument of a [FunctionCall].
 * @param expression expression that holds the content of the argument
 * @param isBody whether this applies to a 'body' parameter.
 *               Parameters which are annotated as body params must come last in the function signature
 */
data class FunctionCallArgument(
    val expression: Expression,
    val isBody: Boolean = false,
) {
    /**
     * The lazily evaluated output value of [expression].
     */
    val value: InputValue<*> by lazy { expression.eval() }
}

/**
 * @return a string representation of a sequence of arguments
 */
fun List<FunctionCallArgument>.asString() = "(" + joinToString { it.value.unwrappedValue.toString() } + ")"
