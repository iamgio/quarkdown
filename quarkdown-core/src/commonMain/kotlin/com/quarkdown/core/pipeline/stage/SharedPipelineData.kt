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
