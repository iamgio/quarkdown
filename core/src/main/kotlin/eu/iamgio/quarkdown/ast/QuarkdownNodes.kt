package eu.iamgio.quarkdown.ast

import eu.iamgio.quarkdown.document.page.PageMarginPosition
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

// Nodes that aren't parsed from the source Markdown input,
// but rather returned from and/or handled by Quarkdown functions.

/**
 * A generic group of block nodes used as input for Quarkdown functions.
 * @see eu.iamgio.quarkdown.function.value.ValueFactory.blockMarkdown
 */
data class MarkdownContent(
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(AstRoot(children))
}

/**
 * A generic group of inline nodes used as input for Quarkdown functions.
 * @see eu.iamgio.quarkdown.function.value.ValueFactory.inlineMarkdown
 */
data class InlineMarkdownContent(
    override val children: InlineContent,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(AstRoot(children))
}

/**
 * An aligned block.
 * @param alignment alignment the content should have
 */
data class Aligned(
    val alignment: Alignment,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Possible alignment types of an [Aligned] block.
     */
    enum class Alignment {
        LEFT,
        CENTER,
        RIGHT,
    }
}

/**
 * A block whose content is clipped in a path.
 * @param clip type of the clip path
 */
data class Clipped(
    val clip: Clip,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Possible clip types of a [Clipped] block.
     */
    enum class Clip {
        CIRCLE,
    }
}

/**
 * A generic box that contains content.
 * @param title box title. If `null`, the box is untitled.
 * @param children content of the box
 */
data class Box(
    val title: InlineContent?,
    override val children: List<Node>,
) : NestableNode {
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
