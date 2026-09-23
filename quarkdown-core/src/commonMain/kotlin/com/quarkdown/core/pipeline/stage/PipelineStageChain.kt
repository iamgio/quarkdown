package com.quarkdown.core.pipeline.stage

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

/**
 * Conditionally chains this pipeline stage with [next] if it is not null.
 *
 * - If [next] is not null, this behaves like [then], chaining the stages together.
 * - If [next] is null, this stage is returned unchanged.
 */
infix fun <A, B> PipelineStage<A, B>.thenOptionally(next: PipelineStage<B, B>?): PipelineStage<A, B> =
    if (next != null) {
        this then next
    } else {
        this
    }
