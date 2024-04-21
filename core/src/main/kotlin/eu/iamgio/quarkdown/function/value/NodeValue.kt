package eu.iamgio.quarkdown.function.value

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.function.value.output.OutputValueVisitor

/**
 * An immutable [Node] [Value].
 */
data class NodeValue(override val unwrappedValue: Node) : OutputValue<Node> {
    override fun <O> accept(visitor: OutputValueVisitor<O>): O = visitor.visit(this)
}

/**
 * @return [this] node wrapped into a [NodeValue]
 */
fun Node.wrappedAsValue() = NodeValue(this)
