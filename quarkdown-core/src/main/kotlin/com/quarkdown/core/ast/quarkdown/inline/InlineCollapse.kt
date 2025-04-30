package com.quarkdown.core.ast.quarkdown.inline

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A collapsible block, whose content can be hidden or shown by interacting with it.
 * @param text expanded content
 * @param placeholder content to show when the node is collapsed
 * @param isOpen whether the node is expanded at the beginning
 */
class InlineCollapse(
    override val text: InlineContent,
    val placeholder: InlineContent,
    val isOpen: Boolean,
) : TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    companion object {
        /**
         * A default placeholder for the collapsed state of a [InlineCollapse].
         */
        const val DEFAULT_PLACEHOLDER = "(...)"
    }
}
