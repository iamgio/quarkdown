package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData
import com.quarkdown.core.rendering.NodeRenderer

/**
 * Pipeline stage responsible for rendering the abstract syntax tree (AST) into a text output.
 *
 * This stage takes an [AstRoot] (produced by the [ParsingStage]) as input and
 * produces a [CharSequence] as output. It uses a [NodeRenderer] to visit the AST
 * and render it into the target format (e.g., HTML).
 */
class RenderingStage(
    private val renderer: NodeRenderer,
) : PipelineStage<AstRoot, CharSequence> {
    override val hook = PipelineHooks::afterRendering

    override fun process(
        input: AstRoot,
        data: SharedPipelineData,
    ): CharSequence = this.renderer.visit(input)
}
