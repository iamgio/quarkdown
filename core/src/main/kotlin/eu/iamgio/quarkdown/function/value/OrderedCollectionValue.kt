package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * An immutable [Value] that contains other values of the same type, ordered.
 * @param T the element type of the list
 */
data class OrderedCollectionValue<T : OutputValue<*>>(override val unwrappedValue: List<T>) : IterableValue<T> {
    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] list wrapped into a [OrderedCollectionValue]
 */
fun <T : OutputValue<*>> List<T>.wrappedAsValue() = OrderedCollectionValue(this)
