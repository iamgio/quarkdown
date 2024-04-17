package eu.iamgio.quarkdown.function.expression.visitor

import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.expression.append
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.EnumValue
import eu.iamgio.quarkdown.function.value.InputValue
import eu.iamgio.quarkdown.function.value.MarkdownContentValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.ObjectValue
import eu.iamgio.quarkdown.function.value.StringValue

/**
 * An [ExpressionVisitor] that evaluates an expression into a single static value,
 * which can be used as an input for another function call.
 */
class EvalExpressionVisitor : ExpressionVisitor<InputValue<*>> {
    // Static values: the evaluation is the value itself.
    override fun visit(value: StringValue) = value

    override fun visit(value: NumberValue) = value

    override fun visit(value: BooleanValue) = value

    override fun visit(value: EnumValue) = value

    override fun visit(value: ObjectValue<*>) = value

    override fun visit(value: MarkdownContentValue) = value

    override fun visit(value: DynamicValue) = value

    // When used as an input value for another function call,
    // the output type of the function call must be an InputValue.
    override fun visit(expression: FunctionCall<*>): InputValue<*> =
        when (val result = expression.execute()) {
            is InputValue<*> -> result
            else -> throw IllegalStateException(expression.function.name + "'s output is not a suitable input value")
        }

    override fun visit(expression: ComposedExpression): InputValue<*> {
        if (expression.expressions.isEmpty()) {
            throw IllegalStateException("Composed expression has no sub-expressions")
        }

        // Creates a single expression out of multiple ones
        // by appending them to each other.
        var merged = expression.expressions.first()
        expression.expressions.asSequence().drop(1).forEach {
            merged = merged.append(it)
        }

        // The value of the built expression.
        return merged.eval()
    }
}
