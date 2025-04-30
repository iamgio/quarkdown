package com.quarkdown.core.ast.quarkdown.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A math (TeX) inline.
 * @param expression expression content
 */
class MathSpan(
    val expression: String,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
