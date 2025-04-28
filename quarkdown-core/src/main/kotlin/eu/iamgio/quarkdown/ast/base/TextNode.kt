package eu.iamgio.quarkdown.ast.base

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node

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
