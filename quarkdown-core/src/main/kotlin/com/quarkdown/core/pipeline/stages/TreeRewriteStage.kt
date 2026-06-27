package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData

/**
 * Pipeline stage that rewrites the AST after function call expansion to apply user-defined
 * extensions to Markdown primitives.
 *
 * Primitive nodes that implement [com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode]
 * (e.g. headings) are wrapped into a [com.quarkdown.core.ast.quarkdown.FunctionCallNode] whenever
 * their backing function has been wrapped via `.extend`, then immediately expanded so the
 * wrapper output replaces the original primitive. This is the mechanism that lets `.extend {heading}`
 * affect both `.heading {...}` calls and plain `#` Markdown syntax.
 *
 * As an optimization, the rewrite is skipped entirely when no extension is registered in the context,
 * which is the common case.
 *
 * @see com.quarkdown.core.ast.iterator.AstRewriter for the underlying traversal logic
 */
object TreeRewriteStage : PipelineStage<AstRoot, AstRoot> {
    override val hook = PipelineHooks::afterTreeRewrite

    override fun process(
        input: AstRoot,
        data: SharedPipelineData,
    ): AstRoot {
        if (!data.context.hasFunctionsExtended()) return input

        return data.context.flavor.treeIteratorFactory
            .rewriter(data.context)
            .traverse(input)
    }
}
