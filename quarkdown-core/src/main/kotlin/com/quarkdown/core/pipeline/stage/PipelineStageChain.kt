package com.quarkdown.core.pipeline.stage

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.pipeline.Pipeline

/**
 * Shared data that is passed between pipeline stages during execution.
 *
 * This data is passed to each stage's [PipelineStage.process] method, allowing stages
 * to access and modify shared state as needed.
 *
 * @param pipeline the pipeline instance executing the stages
 * @param context the mutable context containing state and configuration
 */
data class SharedPipelineData(
    val pipeline: Pipeline,
    val context: MutableContext,
)

/**
 * Chains two pipeline stages together to form a new pipeline stage.
 *
 * Given two stages, `A -> B` and `B -> C`, this operator produces a new stage `A -> C`
 *
 * This operator allows pipeline stages to be composed in a fluent manner:
 * ```
 * val combinedStage = stage1 then stage2 then stage3
 * ```
 *
 * The output of the first stage becomes the input to the second stage,
 * and the resulting pipeline stage takes the input of the first stage
 * and produces the output of the second stage.
 *
 * @param next the next pipeline stage to execute after this one
 * @return a new pipeline stage that executes this stage followed by the next stage
 * @see com.quarkdown.core.pipeline.PipelineChainFactory
 */
infix fun <A, B, C> PipelineStage<A, B>.then(next: PipelineStage<B, C>): PipelineStage<A, C> =
    object : PipelineStage<A, C> {
        override val hook = null

        override fun process(
            input: A,
            data: SharedPipelineData,
        ): C {
            val intermediate = this@then.execute(input, data)
            return next.execute(intermediate, data)
        }
    }
