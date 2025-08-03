package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A node that, when rendered in a `Slides` environment in speaker view,
 * contains speaker notes for the current slide.
 */
class SlidesSpeakerNote(
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
