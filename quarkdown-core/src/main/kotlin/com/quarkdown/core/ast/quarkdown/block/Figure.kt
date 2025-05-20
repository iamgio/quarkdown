package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.SingleChildNestableNode
import com.quarkdown.core.ast.attributes.CaptionableNode
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A block which displays a single child, with an optional caption.
 * If the caption is provided, the block is numbered.
 * @param child wrapped child
 * @param T type of the wrapped child node
 */
abstract class Figure<T : Node>(
    override val child: T,
) : SingleChildNestableNode<T>,
    LocationTrackableNode,
    CaptionableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}

/**
 * An optionally-numbered block which displays a single image, with an optional caption.
 * @param child wrapped image
 * @see Image
 */
class ImageFigure(
    child: Image,
) : Figure<Image>(child) {
    /**
     * Caption of the image, if any. This matches the image title.
     */
    override val caption: String? = child.link.title
}

/**
 * An optionally-numbered block which displays a single Mermaid diagram, with an optional caption.
 * @param child wrapped diagram
 * @param caption optional caption of the diagram
 * @see MermaidDiagram
 */
class MermaidDiagramFigure(
    child: MermaidDiagram,
    override val caption: String? = null,
) : Figure<MermaidDiagram>(child)
