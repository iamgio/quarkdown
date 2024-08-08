package eu.iamgio.quarkdown.ast.base

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.Node

/**
 * A general link node.
 * @see eu.iamgio.quarkdown.ast.base.inline.Link
 * @see eu.iamgio.quarkdown.ast.base.block.LinkDefinition
 */
interface LinkNode : Node {
    /**
     * Inline content of the displayed label.
     */
    val label: InlineContent

    /**
     * URL this link points to.
     */
    val url: String

    /**
     * Optional title.
     */
    val title: String?
}
