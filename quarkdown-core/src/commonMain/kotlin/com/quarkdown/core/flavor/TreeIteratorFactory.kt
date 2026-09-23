package com.quarkdown.core.flavor

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.iterator.AstIterator
import com.quarkdown.core.context.MutableContext

/**
 * Provider of tree iterators.
 * A tree iterator is responsible for traversing the AST and performing operations
 * such as registrations, table of contents generation, etc.
 */
interface TreeIteratorFactory {
    /**
     * @param context the context of the document
     * @return the default tree iterator, used to visit the AST and run side-effecting hooks (e.g. reference resolution, presence detection)
     */
    fun default(context: MutableContext): AstIterator<Unit>

    /**
     * @param context the context of the document
     * @return a tree iterator that produces a rewritten [AstRoot],
     *         used by [com.quarkdown.core.pipeline.stages.TreeRewriteStage] to apply user-defined show-rules
     */
    fun rewriter(context: MutableContext): AstIterator<AstRoot>
}
