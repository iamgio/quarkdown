package com.quarkdown.core.ast.quarkdown

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.util.node.group

/**
 * A node that may have a caption, such as a [com.quarkdown.core.ast.base.block.Table] or a [com.quarkdown.core.ast.quarkdown.block.ImageFigure].
 * The caption is a sequence of inline nodes, which accepts further inline formatting (e.g. emphasis, links).
 *
 * Extends [NestableNode] so that the caption content is reachable by AST tree traversals.
 */
interface CaptionableNode : NestableNode {
    /**
     * The optional caption, as inline content. If `null`, this node has no caption.
     */
    val caption: InlineContent?

    /**
     * Caption nodes grouped into a single container, making them visible to tree traversals.
     * Subclasses that define their own [children] must include this in their override.
     */
    override val children: List<Node>
        get() = listOf(caption.group())
}
