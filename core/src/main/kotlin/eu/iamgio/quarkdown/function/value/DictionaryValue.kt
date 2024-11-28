package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * A mutable [Map] [Value], with string keys and values of type [T].
 * @param T type of values in the dictionary
 */
data class DictionaryValue<T : OutputValue<*>>(
    override val unwrappedValue: MutableMap<String, T>,
) : InputValue<MutableMap<String, T>>,
    OutputValue<MutableMap<String, T>> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * Generates a [DictionaryValue] from key-value pairs.
 * @param pairs key-value pairs
 */
fun dictionaryOf(vararg pairs: Pair<String, OutputValue<*>>) = DictionaryValue(mutableMapOf(*pairs))
