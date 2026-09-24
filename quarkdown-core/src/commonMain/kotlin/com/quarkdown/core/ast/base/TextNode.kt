package com.quarkdown.core.ast.base

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node

/**
 * A node that may contain inline content as its children.
 */
interface TextNode : NestableNode {
    /**
     * The text of the node as processed inline content.
     */
    val text: InlineContent

    override val children: List<Node>
        get() = text
}
