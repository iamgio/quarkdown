package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * An immutable [Set] [Value] that contains other values of the same type, unordered.
 * @param T the element type of the list
 */
data class SetValue<T : OutputValue<*>>(override val unwrappedValue: Set<T>) : IterableValue<T> {
    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] set wrapped into a [SetValue]
 */
fun <T : OutputValue<*>> Set<T>.wrappedAsValue() = SetValue(this)
