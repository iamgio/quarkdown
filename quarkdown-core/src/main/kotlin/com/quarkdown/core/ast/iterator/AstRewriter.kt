package com.quarkdown.core.ast.iterator

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.NestableNode
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode
import com.quarkdown.core.ast.quarkdown.FunctionCallNode
import com.quarkdown.core.ast.rewriter.withChildren
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.errorHandler
import com.quarkdown.core.function.call.FunctionCallNodeExpander
import com.quarkdown.core.pipeline.error.PipelineErrorHandler

/**
 * An [AstIterator] that walks the AST and replaces every [PrimitiveFunctionBackedNode] whose
 * backing function has been wrapped via `.extend` with an equivalent [FunctionCallNode].
 * It queues the synthesized calls on the context, and finally expands them in one pass so the rewritten
 * tree is ready for rendering.
 *
 * The traversal recurses into the children of [FunctionCallNode]s whose backing function is
 * *not* extended (for example, the `.tableofcontents` call output contains a "Table of Contents"
 * heading that should also pick up `.extend {heading}`). [FunctionCallNode]s whose backing
 * function *is* extended are treated as opaque: their children are the wrapper's chosen output
 * and must not be re-rewritten — otherwise the `.super` heading inside an `.extend {heading}`
 * wrapper would be wrapped over and over.
 *
 * @param context context to resolve extensions against and to register synthesized calls in
 * @param errorHandler strategy used by the expander to handle errors raised by wrappers.
 *                     Defaults to the error handler of the pipeline currently attached to [context];
 *                     fails fast if neither is available
 * @see com.quarkdown.core.pipeline.stages.TreeRewriteStage
 */
class AstRewriter(
    private val context: MutableContext,
    errorHandler: PipelineErrorHandler = requireNotNull(context.errorHandler) { "AstRewriter requires a pipeline error handler" },
) : AstIterator<AstRoot> {
    private val expander = FunctionCallNodeExpander(context, errorHandler)

    override fun traverse(root: AstRoot): AstRoot {
        val rewritten = rewriteSubtree(root) as AstRoot
        expander.expandAll()
        return rewritten
    }

    /**
     * Recursively rewrites [parent] and its descendants. Returns [parent] unchanged when no
     * descendant required rewriting; otherwise returns a copy carrying the rewritten children.
     */
    private fun rewriteSubtree(parent: NestableNode): NestableNode {
        if (parent.children.isEmpty()) return parent

        var changed = false
        val newChildren =
            parent.children.map { child ->
                rewriteChild(child).also {
                    if (it !== child) changed = true
                }
            }

        return if (changed) {
            parent.withChildren(newChildren) as NestableNode
        } else {
            parent
        }
    }

    /**
     * Rewrites a single [child], deciding which of the four cases applies:
     * wrap an extended primitive, leave an already-expanded extension wrapper opaque,
     * recurse into anything else nestable, or pass leaves through unchanged.
     */
    private fun rewriteChild(child: Node): Node =
        when (child) {
            // Extended primitive: wrap into a call, expanded later.
            is PrimitiveFunctionBackedNode if context.isFunctionExtended(child.backingFunctionName) -> {
                val withRewrittenChildren =
                    if (child is NestableNode) {
                        rewriteSubtree(child) as PrimitiveFunctionBackedNode
                    } else {
                        child
                    }
                synthesize(withRewrittenChildren)
            }

            // Already-expanded: don't descend.
            is FunctionCallNode if context.isFunctionExtended(child.name) -> {
                child
            }

            // Recurse normally.
            is NestableNode -> {
                rewriteSubtree(child)
            }

            // Leaf.
            else -> {
                child
            }
        }

    /**
     * Materializes [node] as a [FunctionCallNode] referencing the same backing function
     * and registers it on [context] so the next [FunctionCallNodeExpander.expandAll] picks it up.
     */
    private fun synthesize(node: PrimitiveFunctionBackedNode): FunctionCallNode =
        FunctionCallNode(
            context = context,
            name = node.backingFunctionName,
            arguments = node.toFunctionCallArguments(),
            isBlock = node.isBackingCallBlock,
        ).also(context::register)
}
