package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A block quote.
 * @param type information type. If `null`, the quote does not have a particular type
 * @param attribution additional author or source of the quote
 * @param children content
 */
class BlockQuote(
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
