package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A block whose content is clipped in a path.
 * @param clip type of the clip path
 */
class Clipped(
    val clip: Clip,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Possible clip types of a [Clipped] block.
     */
    enum class Clip : RenderRepresentable {
        CIRCLE,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)
    }
}
