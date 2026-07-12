package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A forced page break.
 */
class PageBreak :
    Node,
    PrimitiveFunctionBackedNode {
    override val backingFunctionName: String
        get() = "pagebreak"

    override fun toFunctionCallArguments() = emptyList<FunctionCallArgument>()

    override fun toString() = "PageBreak"

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
