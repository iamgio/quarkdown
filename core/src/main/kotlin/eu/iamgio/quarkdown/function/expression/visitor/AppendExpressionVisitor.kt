package eu.iamgio.quarkdown.function.expression.visitor

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.Text
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.expression.append
import eu.iamgio.quarkdown.function.expression.eval
import eu.iamgio.quarkdown.function.value.BooleanValue
import eu.iamgio.quarkdown.function.value.DynamicValue
import eu.iamgio.quarkdown.function.value.EnumValue
import eu.iamgio.quarkdown.function.value.GeneralCollectionValue
import eu.iamgio.quarkdown.function.value.InlineMarkdownContentValue
import eu.iamgio.quarkdown.function.value.LambdaValue
import eu.iamgio.quarkdown.function.value.MarkdownContentValue
import eu.iamgio.quarkdown.function.value.NumberValue
import eu.iamgio.quarkdown.function.value.ObjectValue
import eu.iamgio.quarkdown.function.value.OrderedCollectionValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.UnorderedCollectionValue
import eu.iamgio.quarkdown.function.value.Value
import eu.iamgio.quarkdown.function.value.VoidValue

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
    /**
     * @return string result of the concatenation between [this] and [other]
     */
    private fun Value<*>.concatenate(): String {
        fun stringify(value: Value<*>) =
            when (value) {
                is VoidValue -> ""
                else -> value.unwrappedValue.toString()
            }
        return stringify(this) + stringify(other.eval())
    }

    // "abc" "def"        -> "abcdef"
    // "abc" .sum {2} {3} -> "abc5"
    override fun visit(value: StringValue) = StringValue(value.concatenate())

    // 15 "abc" -> "15abc"
    // 15 8     -> "158"
    override fun visit(value: NumberValue) = StringValue(value.concatenate())

    // true false -> false
    // false true -> false
    // true true  -> true
    // true "abc" -> "trueabc"
    override fun visit(value: BooleanValue): Expression =
        when (other) {
            // Logic AND between values.
            is BooleanValue -> BooleanValue(value.unwrappedValue && other.unwrappedValue)
            else -> StringValue(value.concatenate())
        }

    // [a, b, c] "abc" -> [a, b, c, "abc"]
    override fun visit(value: OrderedCollectionValue<*>): Expression =
        OrderedCollectionValue(
            value.unwrappedValue + (other.eval() as OutputValue<*>),
        )

    // [a, b, c] "abc" -> [a, b, c, "abc"]
    override fun visit(value: UnorderedCollectionValue<*>): Expression =
        UnorderedCollectionValue(
            value.unwrappedValue + (other.eval() as OutputValue<*>),
        )

    // [a, b, c] "abc" -> [a, b, c, "abc"]
    override fun visit(value: GeneralCollectionValue<*>): Expression =
        GeneralCollectionValue(
            value.unwrappedValue + (other.eval() as OutputValue<*>),
        )

    // CENTER "abc"  -> "CENTERabc"
    // CENTER CENTER -> "CENTERCENTER"
    // CENTER 15     -> "CENTER15"
    override fun visit(value: EnumValue) = StringValue(value.concatenate())

    // obj "abc" -> "objabc"
    override fun visit(value: ObjectValue<*>) = StringValue(value.concatenate())

    private fun concatenateAsNodes(root: Node): List<Node> {
        val nodes = mutableListOf(root)
        // Append node to the sub-AST.
        nodes +=
            when (other) {
                is MarkdownContentValue -> other.unwrappedValue
                else -> Text(other.toString())
            }

        return nodes
    }

    // MarkdownContent(Text("abc")) Text("def") -> MarkdownContent(Text("abc"), Text("abcdef"))
    // MarkdownContent(Text("abc")) "def"       -> MarkdownContent(Text("abc"), Text("abcdef"))
    // MarkdownContent(Text("abc")) 15          -> MarkdownContent(Text("abc"), Text("15"))
    override fun visit(value: MarkdownContentValue): Expression {
        return MarkdownContentValue(MarkdownContent(concatenateAsNodes(value.unwrappedValue)))
    }

    // InlineMarkdownContent(Text("abc")) Text("def") -> InlineMarkdownContent(Text("abc"), Text("abcdef"))
    // InlineMarkdownContent(Text("abc")) "def"       -> InlineMarkdownContent(Text("abc"), Text("abcdef"))
    // InlineMarkdownContent(Text("abc")) 15          -> InlineMarkdownContent(Text("abc"), Text("15"))
    override fun visit(value: InlineMarkdownContentValue): Expression {
        return MarkdownContentValue(MarkdownContent(concatenateAsNodes(value.unwrappedValue)))
    }

    // Like visit(StringValue)
    override fun visit(value: DynamicValue): Expression = DynamicValue(value.concatenate())

    // TODO append lambda
    override fun visit(value: LambdaValue): Expression = throw UnsupportedOperationException()

    // Appends the result of the evaluation.
    override fun visit(expression: FunctionCall<*>): Expression =
        when (val result = expression.eval()) {
            is Expression -> result.append(other)
            else -> StringValue(result.concatenate())
        }

    /**
     * @throws UnsupportedOperationException there is no way a composed expression could be appended to another expression
     */
    override fun visit(expression: ComposedExpression): Expression {
        throw UnsupportedOperationException()
    }
}
