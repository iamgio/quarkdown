package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode
import com.quarkdown.core.ast.attributes.style.NodeStyle
import com.quarkdown.core.ast.attributes.style.StylableNode
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.function.call.FunctionCallArgument
import com.quarkdown.core.function.value.BooleanValue
import com.quarkdown.core.function.value.NoneValue
import com.quarkdown.core.function.value.ObjectValue
import com.quarkdown.core.function.value.StringValue
import com.quarkdown.core.function.value.data.EvaluableString
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A math (TeX) block.
 *
 * A math block can be cross-referenced and can be numbered, as long as it has a [referenceId].
 * @param expression expression content
 * @param referenceId optional reference id for cross-referencing via a [com.quarkdown.core.ast.quarkdown.reference.CrossReference]
 * @param style styling applied to the block
 */
class Math(
    val expression: String,
    override val referenceId: String? = null,
    override val style: NodeStyle = NodeStyle.DEFAULT,
) : LocationTrackableNode,
    CrossReferenceableNode,
    StylableNode,
    PrimitiveFunctionBackedNode {
    /**
     * A math block is numbered if it has a [referenceId].
     */
    override val canTrackLocation: Boolean
        get() = referenceId != null

    override val backingFunctionName: String
        get() = "math"

    override fun toFunctionCallArguments() =
        listOf(
            FunctionCallArgument(name = "content", expression = ObjectValue(EvaluableString(expression))),
            FunctionCallArgument(name = "block", expression = BooleanValue(true)),
            FunctionCallArgument(name = "ref", expression = referenceId?.let(::StringValue) ?: NoneValue),
        )

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
