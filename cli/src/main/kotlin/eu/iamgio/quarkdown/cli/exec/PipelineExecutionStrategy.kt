package eu.iamgio.quarkdown.cli.exec

import eu.iamgio.quarkdown.pipeline.Pipeline

/**
 * A strategy to execute a [Pipeline].
 */
interface PipelineExecutionStrategy {
    /**
     * Executes the [pipeline].
     * @param pipeline pipeline to execute
     */
    fun execute(pipeline: Pipeline)
}
