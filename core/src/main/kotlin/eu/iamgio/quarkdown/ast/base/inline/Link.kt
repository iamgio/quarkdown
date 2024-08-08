package eu.iamgio.quarkdown.ast.base.inline

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.LinkNode
import eu.iamgio.quarkdown.ast.base.block.LinkDefinition
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A link.
 * @param label inline content of the displayed label
 * @param url URL this link points to
 * @param title optional title
 */
data class Link(
    override val label: InlineContent,
    override val url: String,
    override val title: String?,
) : LinkNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * A link that references a [LinkDefinition].
 * @param label inline content of the displayed label
 * @param reference label of the [LinkDefinition] this link points to
 * @param fallback supplier of the node to show instead of [label] in case the reference is invalid
 */
data class ReferenceLink(
    val label: InlineContent,
    val reference: InlineContent,
    val fallback: () -> Node,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
