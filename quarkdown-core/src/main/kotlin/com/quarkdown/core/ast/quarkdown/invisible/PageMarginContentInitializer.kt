package com.quarkdown.core.ast.quarkdown.invisible

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.document.page.PageMarginPosition
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A non-visible node that triggers a property in paged documents that allows displaying content on each page.
 * @param children content to be displayed on each page
 * @param position position of the content within the page
 */
class PageMarginContentInitializer(
    override val children: List<Node>,
    val position: PageMarginPosition,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
