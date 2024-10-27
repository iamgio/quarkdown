package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.attributes.LocationTrackableNode
import eu.iamgio.quarkdown.ast.attributes.id.Identifiable
import eu.iamgio.quarkdown.ast.attributes.id.IdentifierProvider
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A block which displays a single image, with an optional caption.
 * @param image image to display
 */
class ImageFigure(val image: Image) : NestableNode, LocationTrackableNode, Identifiable {
    /**
     * Caption of the image. This matches the image title.
     */
    val caption: String? = image.link.title

    /**
     * A singleton list containing [image].
     * This is needed to allow the image to be traversed by a tree iterator
     * (e.g. for media storage registration)
     */
    override val children: List<Node>
        get() = listOf(image)

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    override fun <T> accept(visitor: IdentifierProvider<T>): T = visitor.visit(this)
}
