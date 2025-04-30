package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A link.
 * @param label inline content of the displayed label
 * @param url URL this link points to
 * @param title optional title
 */
class Link(
    override val label: InlineContent,
    override val url: String,
    override val title: String?,
) : LinkNode, TextNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    override val text: InlineContent
        get() = label
}

/**
 * A link that references a [LinkDefinition].
 * @param label inline content of the displayed label
 * @param reference label of the [LinkDefinition] this link points to
 * @param fallback supplier of the node to show instead of [label] in case the reference is invalid
 * @param onResolve actions to perform when the reference is resolved
 * @see com.quarkdown.core.context.resolveOrFallback
 */
class ReferenceLink(
    val label: InlineContent,
    val reference: InlineContent,
    val fallback: () -> Node,
    val onResolve: MutableList<(resolved: LinkNode) -> Unit> = mutableListOf(),
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
