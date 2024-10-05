package eu.iamgio.quarkdown.ast.base.block

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A block quote.
 * @param type information type. If `null`, the quote does not have a particular type
 * @param attribution additional author or source of the quote
 * @param children content
 */
data class BlockQuote(
    val type: Type? = null,
    val attribution: InlineContent? = null,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Type a [BlockQuote] might have.
     */
    enum class Type : RenderRepresentable {
        TIP,
        NOTE,
        WARNING,
        IMPORTANT,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }
}
