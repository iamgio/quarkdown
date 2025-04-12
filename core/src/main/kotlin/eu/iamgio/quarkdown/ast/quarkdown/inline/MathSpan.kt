package eu.iamgio.quarkdown.ast.quarkdown.inline

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A math (TeX) inline.
 * @param expression expression content
 */
class MathSpan(
    val expression: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
