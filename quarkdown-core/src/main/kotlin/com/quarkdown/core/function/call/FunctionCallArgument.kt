package com.quarkdown.core.function.call

import com.quarkdown.core.function.expression.Expression
import com.quarkdown.core.function.expression.eval
import com.quarkdown.core.function.value.Value

/**
 * An argument of a [FunctionCall].
 * @param expression expression that holds the content of the argument
 * @param name this argument's name, which should match that of a parameter of the called function
 * @param isBody whether this applies to a 'body' parameter, which must come last in the function signature
 */
data class FunctionCallArgument(
    val expression: Expression,
    val name: String? = null,
    val isBody: Boolean = false,
) {
    /**
     * The lazily evaluated output value of [expression].
     */
    val value: Value<*> by lazy { expression.eval() }

    /**
     * Whether this is a named argument.
     */
    val isNamed: Boolean
        get() = name != null
}

/**
 * @return a string representation of [this] argument's value
 */
fun FunctionCallArgument.asString() = value.unwrappedValue.toString()

/**
 * @return a string representation of [this] sequence of arguments
 */
fun List<FunctionCallArgument>.asString() = "(" + joinToString { it.asString() } + ")"
