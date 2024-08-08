package eu.iamgio.quarkdown.ast.quarkdown.invisible

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.document.page.PageMarginPosition
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A non-visible node that triggers a property in paged documents that allows displaying content on each page.
 * @param children content to be displayed on each page
 * @param position position of the content within the page
 */
data class PageMarginContentInitializer(
    override val children: List<Node>,
    val position: PageMarginPosition,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
