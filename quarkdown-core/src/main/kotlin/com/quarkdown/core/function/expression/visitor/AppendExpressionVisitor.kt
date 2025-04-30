package com.quarkdown.core.function.expression.visitor

import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.function.error.internal.InvalidExpressionEvalException
import com.quarkdown.core.function.expression.ComposedExpression
import com.quarkdown.core.function.expression.Expression
import com.quarkdown.core.function.expression.append
import com.quarkdown.core.function.expression.eval
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.DictionaryValue
import com.quarkdown.core.function.value.DynamicValue
import com.quarkdown.core.function.value.EnumValue
import com.quarkdown.core.function.value.GeneralCollectionValue
import com.quarkdown.core.function.value.InlineMarkdownContentValue
import com.quarkdown.core.function.value.InputValue
import com.quarkdown.core.function.value.IterableValue
import com.quarkdown.core.function.value.LambdaValue
import com.quarkdown.core.function.value.MarkdownContentValue
import com.quarkdown.core.function.value.NodeValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.NumberValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.OrderedCollectionValue
import com.quarkdown.core.function.value.OutputValue
import com.quarkdown.core.function.value.PairValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.UnorderedCollectionValue
import com.quarkdown.core.function.value.Value
import com.quarkdown.core.function.value.VoidValue

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
 * The same principle applies to 'block expressions':
 * ```
 * .if {...}
 *     Item 1
 *     .foreach {2..4}
 *         n:
 *         Item .n
 *     Item 5
 * ```
 * The previous example contains a body composed of multiple expressions:
 * - `StringValue(Item 1)`;
 * - `FunctionCall(foreach)` which returns an `IterableValue` of 3 elements;
 * - `StringValue(Item 5)`.
 * After appending these values, the resulting expression is an `IterableValue` (a [GeneralCollectionValue] in particular)
 * which contains: `Item 1`, `Item 2`, `Item 3`, `Item 4`, `Item 5`.
 *
 * @param other expression to append to the visited expression
 * @see ComposedExpression
 */
class AppendExpressionVisitor(
    private val other: Expression,
) : ExpressionVisitor<Expression> {
    private val otherEval by lazy { other.eval() } // Evaluate the next expression.

    /**
     * @return string result of the concatenation between [this] and [other]
     * @throws InvalidExpressionEvalException if either [this] or [other] is a [NodeValue] (see [com.quarkdown.core.function.value.factory.ValueFactory.eval])
     */
    private fun Value<*>.concatenate(): InputValue<*> {
        val otherEval = this@AppendExpressionVisitor.otherEval

        // Whenever a NodeValue appears in a composed expression, it means the expected output is strictly meant to be
        // a pure Markdown output node. Therefore, the thrown error is caught at eval-time and the expression
        // is re-processed as Markdown content.
        // See ValueFactory.eval for more information.
        if (this is NodeValue || otherEval is NodeValue) {
            throw InvalidExpressionEvalException()
        }

        // Void values are ignored.
        if (this is VoidValue) return otherEval as InputValue<*>
        if (otherEval is VoidValue) return this as InputValue<*>

        // If the other value is a collection, add the current value to it as the first element.
        if (otherEval is IterableValue<*> && this is OutputValue<*>) {
            return GeneralCollectionValue(listOf(this, *otherEval.unwrappedValue.toList().toTypedArray()))
        }

        if (this is GeneralCollectionValue<*>) {
            return GeneralCollectionValue(this.unwrappedValue + otherEval.unwrappedValue as OutputValue<*>)
        }

        // Concatenate the string representation of the two values.

        fun stringify(value: Value<*>) =
            when (value) {
                is VoidValue -> ""
                else -> value.unwrappedValue.toString()
            }

        return StringValue(stringify(this) + stringify(otherEval))
    }

    // "abc" "def"        -> "abcdef"
    // "abc" .sum {2} {3} -> "abc5"
    override fun visit(value: StringValue) = value.concatenate()

    // 15 "abc" -> "15abc"
    // 15 8     -> "158"
    override fun visit(value: NumberValue) = value.concatenate()

    // true false -> false
    // false true -> false
    // true true  -> true
    // true "abc" -> "trueabc"
    override fun visit(value: BooleanValue): Expression =
        when (other) {
            // Logic AND between values.
            is BooleanValue -> BooleanValue(value.unwrappedValue && other.unwrappedValue)
            else -> value.concatenate()
        }

    // [a, b, c] "abc" -> [a, b, c, "abc"]
    override fun visit(value: OrderedCollectionValue<*>): Expression =
        OrderedCollectionValue(
            value.unwrappedValue + otherEval as OutputValue<*>,
        )

    // [a, b, c] "abc" -> [a, b, c, "abc"]
    override fun visit(value: UnorderedCollectionValue<*>): Expression =
        UnorderedCollectionValue(
            value.unwrappedValue + otherEval as OutputValue<*>,
        )

    // [a, b, c] "abc" -> [a, b, c, "abc"]
    override fun visit(value: GeneralCollectionValue<*>): GeneralCollectionValue<*> =
        GeneralCollectionValue(
            value.unwrappedValue + otherEval as OutputValue<*>,
        )

    override fun visit(value: PairValue<*, *>): Expression = visit(GeneralCollectionValue(value.unwrappedValue))

    // {a: 1, b: 2} "abc" -> "{a=1, b=2}abc"
    override fun visit(value: DictionaryValue<*>) = value.concatenate()

    // CENTER "abc"  -> "CENTERabc"
    // CENTER CENTER -> "CENTERCENTER"
    // CENTER 15     -> "CENTER15"
    override fun visit(value: EnumValue) = value.concatenate()

    // obj "abc" -> "objabc"
    override fun visit(value: ObjectValue<*>) = value.concatenate()

    // MarkdownContent(Text("abc")) Text("def") -> MarkdownContent(Text("abc"), Text("abcdef"))
    // MarkdownContent(Text("abc")) "def"       -> MarkdownContent(Text("abc"), Text("abcdef"))
    // MarkdownContent(Text("abc")) 15          -> MarkdownContent(Text("abc"), Text("15"))
    override fun visit(value: MarkdownContentValue): Expression =
        GeneralCollectionValue(listOf(value.asNodeValue(), otherEval as OutputValue<*>))

    // InlineMarkdownContent(Text("abc")) Text("def") -> InlineMarkdownContent(Text("abc"), Text("abcdef"))
    // InlineMarkdownContent(Text("abc")) "def"       -> InlineMarkdownContent(Text("abc"), Text("abcdef"))
    // InlineMarkdownContent(Text("abc")) 15          -> InlineMarkdownContent(Text("abc"), Text("15"))
    override fun visit(value: InlineMarkdownContentValue): Expression =
        GeneralCollectionValue(listOf(value.asNodeValue(), otherEval as OutputValue<*>))

    override fun visit(value: NodeValue): Expression = throw InvalidExpressionEvalException()

    // DynamicValue(15) "abc"        -> "15abc"
    // DynamicValue("abc") [1, 2, 3] -> ["abc", 1, 2, 3]
    override fun visit(value: DynamicValue): Expression =
        when (val result = value.concatenate()) {
            is IterableValue<*> -> result
            else -> DynamicValue(result.unwrappedValue)
        }

    override fun visit(value: LambdaValue): Expression = throw UnsupportedOperationException()

    // None "abc" -> "noneabc"
    override fun visit(value: NoneValue) = value.concatenate()

    // Appends the result of the evaluation.
    override fun visit(expression: FunctionCall<*>): Expression =
        when (val result = expression.eval()) {
            is Expression -> result.append(other)
            else -> result.concatenate()
        }

    /**
     * @throws UnsupportedOperationException there is no way a composed expression could be appended to another expression
     */
    override fun visit(expression: ComposedExpression): Expression = throw UnsupportedOperationException()
}
