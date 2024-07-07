package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.document.page.PageMarginPosition
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A non-visible node that triggers a property in paged documents that allows displaying a page counter on each page.
 * @param content action that returns the text of the counter.
 *                Arguments: index of the current page and total amount of pages.
 *                These are strings instead of numbers since the arguments can be placeholders.
 *                e.g. when using PagedJS for HTML rendering, CSS properties `counter(page)` and `counter(pages)` are used.
 * @param position position of the counter within the page
 */
data class PageCounterInitializer(
    val content: (String, String) -> List<Node>,
    val position: PageMarginPosition,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
