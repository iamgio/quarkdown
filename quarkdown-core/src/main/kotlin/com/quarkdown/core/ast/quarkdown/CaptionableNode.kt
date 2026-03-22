package com.quarkdown.core.ast.quarkdown

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node

/**
 * A node that may have a caption, such as a [com.quarkdown.core.ast.base.block.Table] or a [com.quarkdown.core.ast.quarkdown.block.ImageFigure].
 * The caption is a sequence of inline nodes, which accepts further inline formatting (e.g. emphasis, links).
 */
interface CaptionableNode : Node {
    /**
     * The optional caption, as inline content. If `null`, this node has no caption.
     */
    val caption: InlineContent?
}
