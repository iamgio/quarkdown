package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A math (TeX) block.
 *
 * A math block can be cross-referenced and can be numbered, as long as it has a [referenceId].
 * @param expression expression content
 * @param referenceId optional reference id for cross-referencing via a [com.quarkdown.core.ast.quarkdown.reference.CrossReference]
 */
class Math(
    val expression: String,
    override val referenceId: String? = null,
) : LocationTrackableNode,
    CrossReferenceableNode {
    /**
     * A math block is numbered if it has a [referenceId].
     */
    override val canTrackLocation: Boolean
        get() = referenceId != null

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
