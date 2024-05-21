package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.document.page.PageMarginPosition
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

// Nodes introduced by Quarkdown whose rendering does not correspond to a visible output element.

/**
 * A non-visible node that triggers a property in paged documents that allows displaying content on each page.
 * @param text text content to be displayed on each page.
 * @param position position of the content within the page
 */
data class PageMarginContentInitializer(
    val text: String,
    val position: PageMarginPosition,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * A non-visible node that triggers a property in paged documents that allows displaying a page counter on each page.
 * @param text action that returns the text of the counter.
 *             Arguments: index of the current page and total amount of pages.
 *             These are strings instead of numbers since the arguments can be placeholders.
 *             e.g. when using PagedJS for HTML rendering, CSS properties `counter(page)` and `counter(pages)` are used.
 * @param position position of the counter within the page
 */
data class PageCounterInitializer(
    val text: (String, String) -> String,
    val position: PageMarginPosition,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
