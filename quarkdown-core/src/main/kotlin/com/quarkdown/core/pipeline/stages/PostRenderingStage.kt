package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData
import com.quarkdown.core.rendering.PostRenderer
import com.quarkdown.core.rendering.wrap

/**
 * Pipeline stage responsible for post-processing the rendered output.
 *
 * This stage takes a [CharSequence] (produced by the [RenderingStage]) as input and
 * produces a [CharSequence] as output, wrapping the rendered content into a template using a [PostRenderer].
 */
class PostRenderingStage(
    private val postRenderer: PostRenderer,
) : PipelineStage<CharSequence, CharSequence> {
    override val hook = PipelineHooks::afterPostRendering

    override fun process(
        input: CharSequence,
        data: SharedPipelineData,
    ): CharSequence = this.postRenderer.wrap(input)
}
