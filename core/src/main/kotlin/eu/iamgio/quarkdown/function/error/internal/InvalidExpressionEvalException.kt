package eu.iamgio.quarkdown.function.error.internal

import eu.iamgio.quarkdown.function.expression.ComposedExpression
import eu.iamgio.quarkdown.function.expression.Expression
import eu.iamgio.quarkdown.function.value.NodeValue
import eu.iamgio.quarkdown.function.value.ValueFactory

/**
 * An exception thrown when an [Expression] cannot be evaluated.
 * Most commonly, this is thrown when a [NodeValue] appears in a [ComposedExpression],
 * hence the content must be parsed as Markdown instead of expression.
 * @see ValueFactory.eval
 */
class InvalidExpressionEvalException : Exception()
