package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Creation of a referenceable link definition.
 * @param label inline content of the displayed label
 * @param url URL this link points to
 * @param title optional title
 */
class LinkDefinition(
    override val label: InlineContent,
    override val url: String,
    override val title: String?,
) : LinkNode,
    TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Alias for [label].
     */
    override val text: InlineContent
        get() = label
}
