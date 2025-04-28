package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * An empty [Value] with no content.
 */
data object VoidValue : OutputValue<Unit> {
    override val unwrappedValue = Unit

    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}
