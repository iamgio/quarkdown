package eu.iamgio.quarkdown.ast.attributes

import eu.iamgio.quarkdown.ast.Node

/**
 * A node that may have a caption, such as a [eu.iamgio.quarkdown.ast.base.block.Table] or a [eu.iamgio.quarkdown.ast.quarkdown.block.ImageFigure].
 * The caption is a plain text string, which does not accept further inline formatting.
 */
interface CaptionableNode : Node {
    /**
     * The optional caption.
     */
    val caption: String?
}
