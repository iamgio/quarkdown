package com.quarkdown.core.ast.quarkdown.inline

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode
import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.ast.attributes.style.StylableNode
import com.quarkdown.core.function.dsl.functionCallArguments
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A math (TeX) inline.
 * @param expression expression content
 * @param style styling applied to the inline
 */
class MathSpan(
    val expression: String,
    override val style: NodeStyle = NodeStyle.DEFAULT,
) : Node,
    StylableNode,
    PrimitiveFunctionBackedNode {
    override val isBackingCallBlock: Boolean
        get() = false

    override val backingFunctionName: String
        get() = "math"

    override fun toFunctionCallArguments() =
        functionCallArguments {
            arg("content", evaluable(expression))
            arg("block", boolean(false))
        }

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
