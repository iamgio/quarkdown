package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * An immutable [List] [Value] that contains other values of the same type.
 * @param T the element type of the list
 */
data class ListValue<T : OutputValue<*>>(override val unwrappedValue: List<T>) : OutputValue<List<T>> {
    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] node wrapped into a [ListValue]
 */
fun <T : OutputValue<*>> List<T>.wrappedAsValue() = ListValue(this)
