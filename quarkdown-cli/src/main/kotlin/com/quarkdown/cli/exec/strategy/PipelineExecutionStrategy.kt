package com.quarkdown.cli.exec.strategy

import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.output.OutputResource

/**
 * A strategy to execute a [Pipeline].
 */
interface PipelineExecutionStrategy {
    /**
     * Executes the [pipeline].
     * @param pipeline pipeline to execute
     */
    fun execute(pipeline: Pipeline): OutputResource?
}
