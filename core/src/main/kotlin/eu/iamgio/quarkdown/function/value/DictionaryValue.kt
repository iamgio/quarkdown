package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.expression.visitor.ExpressionVisitor
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * A mutable [Map] [Value], with string keys and values of any type, including nested [DictionaryValue]s.
 */
data class DictionaryValue(
    override val unwrappedValue: MutableMap<String, Value<*>>,
) : InputValue<MutableMap<String, *>>,
    OutputValue<MutableMap<String, *>> {
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visit(this)

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}
