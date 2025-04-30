package com.quarkdown.core.function.expression.visitor

import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.expression.ComposedExpression
import com.quarkdown.core.function.expression.Expression
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.EnumValue
import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.InlineMarkdownContentValue
import com.quarkdown.core.function.value.LambdaValue
import com.quarkdown.core.function.value.MarkdownContentValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.OrderedCollectionValue
import com.quarkdown.core.function.value.PairValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.UnorderedCollectionValue

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

    fun visit(value: PairValue<*, *>): T

    fun visit(value: DictionaryValue<*>): T

    fun visit(value: EnumValue): T

    fun visit(value: ObjectValue<*>): T

    fun visit(value: MarkdownContentValue): T

    fun visit(value: InlineMarkdownContentValue): T

    fun visit(value: NodeValue): T

    fun visit(value: DynamicValue): T

    fun visit(value: LambdaValue): T

    fun visit(value: NoneValue): T

    fun visit(expression: FunctionCall<*>): T

    fun visit(expression: ComposedExpression): T
}
