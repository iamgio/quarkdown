package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.SingleChildNestableNode
import eu.iamgio.quarkdown.ast.attributes.CaptionableNode
import eu.iamgio.quarkdown.ast.attributes.LocationTrackableNode
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A block which displays a single image, with an optional caption.
 * @param child wrapped image
 */
class ImageFigure(
    override val child: Image,
) : SingleChildNestableNode<Image>,
    LocationTrackableNode,
    CaptionableNode {
    /**
     * Caption of the image, if any. This matches the image title.
     */
    override val caption: String? = child.link.title

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
