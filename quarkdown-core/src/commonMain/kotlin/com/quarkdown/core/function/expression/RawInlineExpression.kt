package com.quarkdown.core.function.expression

import com.quarkdown.core.context.Context
import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.factory.ValueFactory

/**
 * An [Expression] wrapping the raw string of an inline function-call argument.
 *
 * When evaluated, it delegates to [ValueFactory.safeExpression], preserving the current
 * eager evaluation behavior. The [raw] string, however, remains accessible so that consumers
 * (for example, [com.quarkdown.core.function.call.binding.RegularArgumentsBinder]) can route
 * the argument through a different parsing strategy when the target parameter type demands it,
 * as is the case for [com.quarkdown.core.function.value.data.Lambda]-typed parameters.
 *
 * @param raw raw source text of the inline argument
 * @param context context used when the raw string is eventually parsed as an expression
 */
class RawInlineExpression(
    val raw: String,
    context: Context,
) : Expression {
    private val delegate: Expression by lazy { ValueFactory.safeExpression(raw, context) }

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = delegate.accept(visitor)
}
