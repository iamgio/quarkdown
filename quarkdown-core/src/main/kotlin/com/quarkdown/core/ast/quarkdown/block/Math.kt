package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A math (TeX) block.
 * @param expression expression content
 * @param referenceId optional reference id for cross-referencing via a [com.quarkdown.core.ast.quarkdown.reference.CrossReference]
 */
class Math(
    val expression: String,
    override val referenceId: String? = null,
) : CrossReferenceableNode {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
