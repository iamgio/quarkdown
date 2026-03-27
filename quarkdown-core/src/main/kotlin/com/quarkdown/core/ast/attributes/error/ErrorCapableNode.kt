package com.quarkdown.core.ast.attributes.error

import com.quarkdown.core.ast.Node
import com.quarkdown.core.pipeline.error.PipelineErrorHandler
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A [Node] that can hold an error which occurred during its processing (e.g. function call expansion).
 * When an error is present, [accept] accepts the error as a node (via [asNode]) instead of the node itself,
 * allowing graceful inline error reporting in the output document.
 */
interface ErrorCapableNode : Node {
    /**
     * The error that occurred during processing, paired with the [PipelineErrorHandler] strategy to handle it.
     * If `null`, no error occurred and the node is rendered normally via [acceptOnSuccess].
     */
    var error: Pair<Throwable, PipelineErrorHandler>?

    /**
     * Visits this node with the given [visitor] when no error is present.
     * @param visitor the visitor to accept
     * @return the result of visiting this node
     */
    fun <T> acceptOnSuccess(visitor: NodeVisitor<T>): T

    /**
     * Accepts the error as a node if an error is present, otherwise accepts this node normally via [acceptOnSuccess].
     * @param visitor the visitor to accept
     * @return the result of visiting the error node or this node
     */
    override fun <T> accept(visitor: NodeVisitor<T>): T =
        error
            ?.let { (throwable, handler) -> throwable.asNode(handler) }
            ?.accept(visitor)
            ?: acceptOnSuccess(visitor)
}
