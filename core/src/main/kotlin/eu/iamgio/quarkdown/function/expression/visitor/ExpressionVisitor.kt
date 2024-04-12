package eu.iamgio.quarkdown.function.expression.visitor

import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.value.DynamicInputValue
import eu.iamgio.quarkdown.function.value.EnumValue
import eu.iamgio.quarkdown.function.value.MarkdownContentValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue

/**
 * A visitor for different kinds of [Expression].
 * @param T output type of the `visit` methods
 * @see Expression
 * @see EvalExpressionVisitor
 * @see AppendExpressionVisitor
 */
interface ExpressionVisitor<T> {
    fun visit(value: StringValue): T

    fun visit(value: NumberValue): T

    fun visit(value: EnumValue): T

    fun visit(value: MarkdownContentValue): T

    fun visit(value: DynamicInputValue): T

    fun visit(expression: FunctionCall<*>): T

    fun visit(expression: ComposedExpression): T
}
