package eu.iamgio.quarkdown.ast.base.inline

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.base.LinkNode
import eu.iamgio.quarkdown.ast.base.block.LinkDefinition
import eu.iamgio.quarkdown.document.size.Size
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * An image.
 * @param link the link the image points to
 * @param width optional width constraint
 * @param height optional height constraint
 */
class Image(
    val link: LinkNode,
    val width: Size?,
    val height: Size?,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}

/**
 * An images that references a [LinkDefinition].
 * @param link the link the image references
 * @param width optional width constraint
 * @param height optional height constraint
 */
class ReferenceImage(
    val link: ReferenceLink,
    val width: Size?,
    val height: Size?,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
