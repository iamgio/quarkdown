package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * An immutable [Value] that contains two elements, which can be iterated.
 * When a [DictionaryValue] is iterated, it's equivalent to a list of key-value pairs.
 * @param F type of the first element
 * @param S type of the second element
 */
data class PairValue<F : OutputValue<*>, S : OutputValue<*>>(private val pairUnwrappedValue: Pair<F, S>) : IterableValue<OutputValue<*>> {
    /**
     * A list of the two elements.
     */
    override val unwrappedValue: Iterable<OutputValue<*>>
        get() = listOf(pairUnwrappedValue.first, pairUnwrappedValue.second)

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}
