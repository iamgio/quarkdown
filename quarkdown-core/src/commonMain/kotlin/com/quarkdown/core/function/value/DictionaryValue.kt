package com.quarkdown.core.function.value

import com.quarkdown.core.function.expression.visitor.ExpressionVisitor
import com.quarkdown.core.function.value.output.OutputValueVisitor

/**
 * A mutable [Map] [Value], with string keys and values of type [T].
 * A dictionary can be adapted to an iterable list of key-value entries.
 * @param T type of values in the dictionary
 */
data class DictionaryValue<T : OutputValue<*>>(
    override val unwrappedValue: MutableMap<String, T>,
) : InputValue<MutableMap<String, T>>,
    OutputValue<MutableMap<String, T>>,
    AdaptableValue<IterableValue<PairValue<StringValue, T>>> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)

    override fun adapt(): IterableValue<PairValue<StringValue, T>> {
        val pairs = unwrappedValue.entries.map { PairValue(it.key.wrappedAsValue() to it.value) }
        return GeneralCollectionValue(pairs)
    }
}

/**
 * Generates a [DictionaryValue] from key-value pairs.
 * @param pairs key-value pairs
 */
fun dictionaryOf(vararg pairs: Pair<String, OutputValue<*>>) = DictionaryValue(mutableMapOf(*pairs))

/**
 * Generates a [DictionaryValue] from key-value pairs.
 * @param pairs key-value pairs
 */
fun dictionaryOf(pairs: Iterable<Pair<String, OutputValue<*>>>) = DictionaryValue(mutableMapOf(*pairs.toList().toTypedArray()))
