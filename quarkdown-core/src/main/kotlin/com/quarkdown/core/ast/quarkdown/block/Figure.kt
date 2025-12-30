package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.SingleChildNestableNode
import com.quarkdown.core.ast.attributes.localization.LocalizedKind
import com.quarkdown.core.ast.attributes.localization.LocalizedKindKeys
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.quarkdown.CaptionableNode
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A block which displays a single child, with an optional caption.
 * If a [caption] is provided or [referenceId] is set, the block is numbered.
 * @param child wrapped child
 * @param caption optional caption of the figure block
 * @param referenceId optional ID that can be cross-referenced via a [com.quarkdown.core.ast.quarkdown.reference.CrossReference]
 * @param T type of the wrapped child node
 */
open class Figure<T : Node>(
    override val child: T,
    override val caption: String? = null,
    override val referenceId: String? = null,
) : SingleChildNestableNode<T>,
    LocationTrackableNode,
    CrossReferenceableNode,
    CaptionableNode,
    LocalizedKind {
    override val kindLocalizationKey: String
        get() = LocalizedKindKeys.FIGURE

    /**
     * A figure is numbered if it has either a [caption] or a [referenceId].
     */
    override val canTrackLocation: Boolean
        get() = caption != null || referenceId != null

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}

/**
 * An optionally-numbered block which displays a single image, with an optional caption.
 * The caption of the image matches the image title, if any.
 * @param child wrapped image
 * @see Image
 */
class ImageFigure(
    child: Image,
) : Figure<Image>(
        child,
        caption = child.link.title,
        referenceId = child.referenceId,
    )
