package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PeekPipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData

/**
 * Peek stage that runs after all rendering has been completed.
 *
 * - It usually matches with [PipelineHooks.afterPostRendering].
 * - If post-rendering is disabled by [com.quarkdown.core.pipeline.PipelineOptions.wrapOutput] set to `false`,
 *   it will match with [PipelineHooks.afterRendering] instead.
 *
 * This stage allows for consistently hooking into the final rendered output,
 * regardless of whether post-rendering is enabled or not.
 */
object AfterAllRenderingPeek : PeekPipelineStage<CharSequence> {
    override val hook = PipelineHooks::afterAllRendering

    override fun peek(
        input: CharSequence,
        data: SharedPipelineData,
    ) {}
}
