package com.quarkdown.core.ast.base.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A reference to a [com.quarkdown.core.ast.base.block.FootnoteDefinition].
 * @param label reference label that should match that of the footnote definition
 * @param fallback supplier of the node to show instead of [label] in case the reference is invalid
 */
class ReferenceFootnote(
    val label: String,
    val fallback: () -> Node,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
