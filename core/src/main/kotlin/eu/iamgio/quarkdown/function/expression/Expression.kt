package eu.iamgio.quarkdown.function.expression

import eu.iamgio.quarkdown.function.value.InputValue

/**
 * An expression that can be evaluated into a single static value.
 * Expressions are used in function arguments.
 */
interface Expression {
    /**
     * @return this expression, evaluated into a single static value
     *         which can be chained as an input for another function
     */
    fun eval(): InputValue<*>

    /**
     * Chains two expressions together, which is used in [ComposedExpression]s.
     * @param other expression to append
     * @return an expression that contains this expression and [other], in order
     */
    fun append(other: Expression): Expression // TODO change to visitor
}
