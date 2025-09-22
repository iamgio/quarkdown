package com.quarkdown.core.function.expression

import com.quarkdown.core.function.expression.visitor.ExpressionVisitor

/**
 * An [Expression] composed by multiple sub-expressions.
 *
 * For example, in the Quarkdown source:
 * `.somefunction {three plus two is .sum {3} {2} and three minus two is .subtract {3} {2}}`
 * The argument to `somefunction` holds a composed expression built by these sub-expressions:
 * - `StringValue(three plus two is )`
 * - `FunctionCall(sum, 3, 2)`
 * - `StringValue( and three minus two is )`
 * - `FunctionCall(subtract, 3, 2)`
 *
 * @param expressions sub-expressions
 */
data class ComposedExpression(
    val expressions: List<Expression>,
) : Expression {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)
}
