package com.quarkdown.core.ast.attributes.primitive

import com.quarkdown.core.ast.Node
import com.quarkdown.core.function.call.FunctionCallArgument

/**
 * A node backed by a primitive function: the node can be wrapped into a call to that function,
 * so any `.extend` registered for the function name is applied to the node's rendering.
 *
 * For example, a `Heading` is backed by `.heading`, so wrapping `.extend {heading}` affects both
 * `.heading {...}` calls and Markdown `#` syntax. See stdlib's `Primitives` module for more primitive functions.
 *
 * The wrapping is performed by [com.quarkdown.core.ast.iterator.AstRewriter] during the
 * [com.quarkdown.core.pipeline.stages.TreeRewriteStage], and only happens when the backing
 * function has actually been extended.
 *
 * @see com.quarkdown.core.pipeline.stages.TreeRewriteStage
 */
interface PrimitiveFunctionBackedNode : Node {
    /**
     * Whether the synthesized backing call should be treated as a block call.
     * Defaults to `true`; inline primitive nodes should override to `false`
     * so the inline output mapper is used during expansion.
     */
    val isBackingCallBlock: Boolean
        get() = true

    /**
     * The name of the function that backs this node, from a loaded library.
     */
    val backingFunctionName: String

    /**
     * Materializes this node's properties into the arguments of the backing function call.
     * Argument names must match the parameter names of the function identified by [backingFunctionName].
     */
    fun toFunctionCallArguments(): List<FunctionCallArgument>
}
