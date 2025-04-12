package eu.iamgio.quarkdown.cli.exec.strategy

import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.output.OutputResource

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
