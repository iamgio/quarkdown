package eu.iamgio.quarkdown.function.expression

import eu.iamgio.quarkdown.function.value.InputValue

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
data class ComposedExpression(private val expressions: List<Expression>) : Expression {
    override fun eval(): InputValue<*> {
        if (expressions.isEmpty()) {
            throw IllegalStateException("Composed expression has no sub-expressions")
        }

        // Create a single expression out of multiple ones
        // by appending them to each other.
        var expression = expressions.first()
        expressions.asSequence().drop(1).forEach {
            expression = expression.append(it)
        }

        // The value of the built expression.
        return expression.eval()
    }

    /**
     * @throws UnsupportedOperationException no way a composed expression could be appended to another expression
     */
    override fun append(other: Expression): Expression = throw UnsupportedOperationException()
}
