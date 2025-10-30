package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PeekPipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData

/**
 * Pipeline stage responsible for traversing the abstract syntax tree (AST).
 *
 * This stage uses a tree iterator to traverse the AST and perform operations on it.
 *
 * @see com.quarkdown.core.context.hooks for tree traversal hooks.
 */
object TreeTraversalStage : PeekPipelineStage<AstRoot> {
    override val hook = PipelineHooks::afterTreeTraversal

    override fun peek(
        input: AstRoot,
        data: SharedPipelineData,
    ) {
        data.context.flavor.treeIteratorFactory
            .default(data.context)
            .traverse(input)
    }
}
