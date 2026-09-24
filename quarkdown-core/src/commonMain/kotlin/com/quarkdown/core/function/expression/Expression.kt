package com.quarkdown.core.function.expression

import com.quarkdown.core.function.expression.visitor.AppendExpressionVisitor
import com.quarkdown.core.function.expression.visitor.EvalExpressionVisitor
import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.Value

/**
 * An expression that can be evaluated into a single static value.
 * Expressions are used in function arguments.
 */
interface Expression {
    /**
     * Accepts a visitor.
     * @param T output type of the visitor
     * @return output of the visit operation
     */
    fun <T> accept(visitor: ExpressionVisitor<T>): T
}

/**
 * @return this expression, evaluated into a single static value
 *         which can be chained as an input for another function
 * @see EvalExpressionVisitor
 */
fun Expression.eval(): Value<*> = this.accept(EvalExpressionVisitor())

/**
 * Chains two expressions together, which is used in [ComposedExpression]s.
 * @param other expression to append
 * @return an expression that contains this expression and [other], in order
 * @see AppendExpressionVisitor
 */
fun Expression.append(other: Expression): Expression = this.accept(AppendExpressionVisitor(other))
