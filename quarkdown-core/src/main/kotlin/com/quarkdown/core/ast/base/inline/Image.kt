package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.attributes.error.ErrorCapableNode
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.block.LinkDefinition
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * An image.
 * @param link the link the image points to
 * @param width optional width constraint
 * @param height optional height constraint
 * @param referenceId optional ID that can be cross-referenced via a [com.quarkdown.core.ast.quarkdown.reference.CrossReference]
 * @param usesMediaStorage whether this image should be registered in the media storage by [com.quarkdown.core.context.hooks.MediaStorerHook],
 *                         if the media storage is enabled in the context
 */
class Image(
    val link: LinkNode,
    val width: Size?,
    val height: Size?,
    override val referenceId: String? = null,
    val usesMediaStorage: Boolean = true,
) : CrossReferenceableNode,
    ErrorCapableNode {
    /**
     * Any error associated with the link will be surfaced as an error on the image itself.
     */
    override var error by link::error

    override fun <T> acceptOnSuccess(visitor: NodeVisitor<T>) = visitor.visit(this)

    /**
     * Creates a copy of this image with the given [link].
     */
    fun copy(link: LinkNode) =
        Image(
            link = link,
            width = width,
            height = height,
            referenceId = referenceId,
            usesMediaStorage = usesMediaStorage,
        )
}

/**
 * An images that references a [LinkDefinition].
 * @param link the link the image references
 * @param width optional width constraint
 * @param height optional height constraint
 * @param referenceId optional ID that can be cross-referenced via a [com.quarkdown.core.ast.quarkdown.reference.CrossReference]
 */
class ReferenceImage(
    val link: ReferenceLink,
    val width: Size?,
    val height: Size?,
    override val referenceId: String? = null,
) : CrossReferenceableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
