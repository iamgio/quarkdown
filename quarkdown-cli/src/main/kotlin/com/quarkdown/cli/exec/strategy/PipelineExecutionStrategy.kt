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
     * @return the generated output resource, if any
     */
    fun execute(pipeline: Pipeline): OutputResource?
}
