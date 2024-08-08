package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.base.LinkNode
import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * Creation of a link reference.
 * @param label inline content of the displayed label
 * @param url URL this link points to
 * @param title optional title
 */
data class LinkDefinition(
    override val label: InlineContent,
    override val url: String,
    override val title: String?,
) : LinkNode, TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Alias for [label].
     */
    override val text: InlineContent
        get() = label
}
