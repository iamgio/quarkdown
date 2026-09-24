package com.quarkdown.core.ast.base.block

import com.quarkdown.amber.annotations.Diverge
import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode
import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.ast.attributes.style.StylableNode
import com.quarkdown.core.ast.base.TextNode
import com.quarkdown.core.function.dsl.functionCallArguments
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A general paragraph.
 * @param text text content
 */
class Paragraph(
    @Diverge override val text: InlineContent,
    override val style: NodeStyle = NodeStyle.DEFAULT,
) : TextNode,
    StylableNode,
    PrimitiveFunctionBackedNode {
    override val backingFunctionName: String = "paragraph"

    override fun toFunctionCallArguments() =
        functionCallArguments {
            arg("content", inline(text))
        }

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
