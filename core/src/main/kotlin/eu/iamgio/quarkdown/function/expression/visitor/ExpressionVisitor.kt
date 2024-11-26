package eu.iamgio.quarkdown.function.expression.visitor

import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DictionaryValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.EnumValue
import eu.iamgio.quarkdown.function.value.GeneralCollectionValue
import eu.iamgio.quarkdown.function.value.InlineMarkdownContentValue
import eu.iamgio.quarkdown.function.value.LambdaValue
import eu.iamgio.quarkdown.function.value.MarkdownContentValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.ObjectValue
import eu.iamgio.quarkdown.function.value.OrderedCollectionValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.UnorderedCollectionValue

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

    fun visit(value: BooleanValue): T

    fun visit(value: OrderedCollectionValue<*>): T

    fun visit(value: UnorderedCollectionValue<*>): T

    fun visit(value: GeneralCollectionValue<*>): T

    fun visit(value: DictionaryValue): T

    fun visit(value: EnumValue): T

    fun visit(value: ObjectValue<*>): T

    fun visit(value: MarkdownContentValue): T

    fun visit(value: InlineMarkdownContentValue): T

    fun visit(value: DynamicValue): T

    fun visit(value: LambdaValue): T

    fun visit(expression: FunctionCall<*>): T

    fun visit(expression: ComposedExpression): T
}
