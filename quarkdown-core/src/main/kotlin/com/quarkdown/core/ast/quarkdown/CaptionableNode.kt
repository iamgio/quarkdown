package com.quarkdown.core.ast.quarkdown

import com.quarkdown.core.ast.Node

/**
 * A node that may have a caption, such as a [com.quarkdown.core.ast.base.block.Table] or a [com.quarkdown.core.ast.quarkdown.block.ImageFigure].
 * The caption is a plain text string, which does not accept further inline formatting.
 */
interface CaptionableNode : Node {
    /**
     * The optional caption.
     */
    val caption: String?
}
