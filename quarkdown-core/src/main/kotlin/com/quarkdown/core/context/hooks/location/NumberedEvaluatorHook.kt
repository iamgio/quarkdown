package com.quarkdown.core.context.hooks.location

import com.quarkdown.core.ast.attributes.location.getLocationLabel
import com.quarkdown.core.ast.iterator.AstIteratorHook
import com.quarkdown.core.ast.iterator.ObservableAstIterator
import com.quarkdown.core.ast.quarkdown.block.Numbered
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.errorHandler
import com.quarkdown.core.pipeline.error.PipelineException

/**
 * Hook that evaluates the [Numbered] nodes in the document.
 * If the evaluation fails, it attaches an error box, as in a regular function call expansion.
 * This needs to be attached **after** the [LocationAwareLabelStorerHook] has populated the location labels.
 * @param context context to retrieve the location label from
 * @see Numbered to understand why it needs evaluation
 */
class NumberedEvaluatorHook(
    private val context: Context,
) : AstIteratorHook {
    override fun attach(iterator: ObservableAstIterator) {
        iterator.on<Numbered> { node ->
            val label = node.getLocationLabel(context) ?: ""

            try {
                node.children = node.childrenSupplier(label)
            } catch (e: PipelineException) {
                val errorHandler = context.errorHandler ?: throw e
                node.error = e to errorHandler
            }
        }
    }
}
