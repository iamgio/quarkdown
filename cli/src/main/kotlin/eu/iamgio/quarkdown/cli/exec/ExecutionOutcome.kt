package eu.iamgio.quarkdown.cli.exec

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import java.io.File

/**
 * Outcome of a pipeline execution.
 * @param resource the output resource produced by the pipeline, if any
 * @param directory output directory, if any
 * @param pipeline the executed pipeline
 * @see runQuarkdown
 */
data class ExecutionOutcome(
    val resource: OutputResource?,
    val directory: File?,
    val pipeline: Pipeline,
) {
    /**
     * The context of the pipeline.
     */
    val context: Context
        get() = pipeline.readOnlyContext
}
