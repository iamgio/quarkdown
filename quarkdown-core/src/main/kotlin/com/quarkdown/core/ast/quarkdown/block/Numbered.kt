package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.attributes.location.SectionLocation
import com.quarkdown.core.document.numbering.DocumentNumbering
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Node that can be numbered depending on its location in the document
 * and the amount of occurrences according to its [key].
 *
 * This node is peculiar, as it's the only node whose children are not evaluated directly during the function call expansion stage,
 * but rather during the AST traversal.
 *
 * This is because in order to evaluate the children, we need to know the location of the node in the document,
 * which is not known until the AST is fully traversed by [LocationAwareLabelStorerHook].
 *
 * After the traversal, the [NumberedEvaluatorHook] will evaluate and assign the [children] of this node, ready to be rendered.
 *
 * Since the evaluation does not happen within [com.quarkdown.core.function.call.FunctionCallNodeExpander],
 * errors thrown during the evaluation will have to be caught externally. This is handled by the hook itself,
 * which append an error box (the same produced from the expander) to [children].
 * From the user's perspective, this does not have any effect.
 * @param key name to group (and count) numbered nodes
 * @param childrenSupplier supplier of the node content given the evaluated [SectionLocation], formatted according to the active [DocumentNumbering]
 * @see com.quarkdown.core.context.hooks.location.LocationAwareLabelStorerHook for storing locations
 * @see com.quarkdown.core.context.hooks.location.NumberedEvaluatorHook for evaluating [childrenSupplier]
 * @see com.quarkdown.core.document.numbering.NumberingFormat
 */
class Numbered(
    val key: String,
    internal val childrenSupplier: (location: String) -> List<Node>,
) : NestableNode,
    LocationTrackableNode {
    override var children: List<Node> = emptyList()

    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
