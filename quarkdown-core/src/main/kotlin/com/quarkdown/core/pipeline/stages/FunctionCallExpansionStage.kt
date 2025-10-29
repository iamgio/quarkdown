package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.function.call.FunctionCallNodeExpander
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PeekPipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData

/**
 * Pipeline stage responsible for expanding function calls in the abstract syntax tree (AST).
 *
 * This stage traverses the AST and expands any function calls found in the document.
 *
 * Function calls are special constructs in the document that invoke functions defined
 * in libraries. These functions can generate content, modify the document structure,
 * or perform other operations.
 *
 * This stage is crucial for implementing the extensibility of the document format,
 * allowing users to define and use custom functions in their documents.
 */
object FunctionCallExpansionStage : PeekPipelineStage<AstRoot> {
    override val hook = PipelineHooks::afterExpanding

    override fun peek(
        input: AstRoot,
        data: SharedPipelineData,
    ) {
        FunctionCallNodeExpander(
            data.context,
            errorHandler = data.pipeline.options.errorHandler,
        ).expandAll()
    }
}
