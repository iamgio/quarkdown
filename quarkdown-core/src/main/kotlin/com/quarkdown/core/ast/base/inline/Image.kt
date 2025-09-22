package com.quarkdown.core.ast.base.inline

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
 */
class Image(
    val link: LinkNode,
    val width: Size?,
    val height: Size?,
    override val referenceId: String? = null,
) : CrossReferenceableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
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
