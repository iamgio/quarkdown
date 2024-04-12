package eu.iamgio.quarkdown.function.expression.visitor

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.value.DynamicInputValue
import eu.iamgio.quarkdown.function.value.EnumValue
import eu.iamgio.quarkdown.function.value.MarkdownContentValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.StringValue

/**
 * An [ExpressionVisitor] that describes the way two expressions are joined together.
 *
 * For example, in the Quarkdown source:
 * `.somefunction {three plus two is .sum {3} {2}}`
 * The argument to `somefunction` is a [ComposedExpression] built by these sub-expressions:
 * - `StringValue(three plus two is )`
 * - `FunctionCall(sum, 3, 2)`
 * After the evaluation of the `sum` call (handled by [EvalExpressionVisitor]) has been executed,
 * the output values are:
 * - `StringValue(three plus two is )`
 * - `NumberValue(5)`
 * These two values are then joined together by this [AppendExpressionVisitor], producing:
 * `StringValue(three plus two is 5)`
 *
 * @param other expression to append to the visited expression
 * @see ComposedExpression
 */
class AppendExpressionVisitor(private val other: Expression) : ExpressionVisitor<Expression> {
    override fun visit(value: StringValue): Expression =
        StringValue(
            value.unwrappedValue +
                other.eval().unwrappedValue.toString(),
        )

    override fun visit(value: NumberValue): Expression =
        StringValue(
            value.unwrappedValue.toString() +
                other.eval().unwrappedValue.toString(),
        )

    override fun visit(value: EnumValue): Expression =
        StringValue(
            value.unwrappedValue.toString() +
                other.eval().unwrappedValue.toString(),
        )

    override fun visit(value: MarkdownContentValue): Expression {
        val nodes = mutableListOf<Node>(value.unwrappedValue)
        // Append node to the sub-AST.
        nodes +=
            when (other) {
                is MarkdownContentValue -> other.unwrappedValue
                else -> Text(other.toString())
            }

        return MarkdownContentValue(MarkdownContent(nodes))
    }

    override fun visit(value: DynamicInputValue): Expression =
        DynamicInputValue(
            value.unwrappedValue +
                other.eval().unwrappedValue.toString(),
        )

    override fun visit(expression: FunctionCall<*>): Expression =
        StringValue(
            expression.eval().unwrappedValue.toString() +
                other.eval().unwrappedValue.toString(),
        )

    /**
     * @throws UnsupportedOperationException there is no way a composed expression
     *         could be appended to another expression
     */
    override fun visit(expression: ComposedExpression): Expression {
        throw UnsupportedOperationException()
    }
}
