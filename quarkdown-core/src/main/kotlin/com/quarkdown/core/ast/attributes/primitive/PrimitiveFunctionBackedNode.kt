package com.quarkdown.core.ast.attributes.primitive

import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.function.call.FunctionCallArgument

/**
 * A node backed by a primitive function: the node is wrapped into a call to
 * that function, so any `.extend` registered for the function name is applied to the node's rendering.
 *
 * For example, a `Heading` is backed by `.heading`, so wrapping `.extend {heading}` affects both
 * `.heading {...}` calls and Markdown `#` syntax. See stdlib's `Primitives` module for more primitive functions.
 *
 * The wrapped call carries the original node as its delegator, allowing the expander to skip the
 * function-call machinery entirely when no extension is registered for the backing function.
 *
 * @see com.quarkdown.core.pipeline.stages.ParsingStage
 */
interface PrimitiveFunctionBackedNode : Node {
    /**
     * Materializes this node's properties into the arguments of the backing function call.
     * Argument names must match the parameter names of the function identified by name.
     * @return a pair of the function name, from a loaded library, and a list of arguments to call it with
     */
    fun toFunctionCall(): Pair<String, List<FunctionCallArgument>>
}

/**
 * If [this] node is a [PrimitiveFunctionBackedNode], returns a [FunctionCallNode] that wraps it
 * (with the original node as its delegator) and registers the call on [context] for later expansion;
 * otherwise returns [this] unchanged.
 *
 * Called from every spot that produces freshly parsed nodes so that primitive wrapping reaches
 * nested content too (e.g. a heading inside a blockquote or list), not just top-level children.
 *
 * @param context context that the new call is parsed under and registered for expansion in
 * @param isBlock whether the wrap is being applied within a block-level parse, carried into the
 *                resulting [FunctionCallNode] so the expander picks the right value->node mapper
 */
internal fun Node.wrapIfPrimitive(
    context: MutableContext,
    isBlock: Boolean,
): Node {
    if (this !is PrimitiveFunctionBackedNode) return this
    val (name, arguments) = toFunctionCall()
    return FunctionCallNode(
        context = context,
        name = name,
        arguments = arguments,
        isBlock = isBlock,
        delegator = this,
    ).also(context::register)
}
