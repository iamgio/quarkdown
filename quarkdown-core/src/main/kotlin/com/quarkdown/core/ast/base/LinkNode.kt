package com.quarkdown.core.ast.base

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node

/**
 * A general link node.
 * @see com.quarkdown.core.ast.base.inline.Link
 * @see com.quarkdown.core.ast.base.block.LinkDefinition
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
