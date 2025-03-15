package eu.iamgio.quarkdown.cli.exec

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.pipeline.Pipeline
import java.io.File

/**
 * Outcome of a pipeline execution.
 * @param directory output directory, if any
 * @param pipeline the executed pipeline
 * @see runQuarkdown
 */
data class ExecutionOutcome(
    val directory: File?,
    val pipeline: Pipeline,
) {
    /**
     * The context of the pipeline.
     */
    val context: Context
        get() = pipeline.readOnlyContext
}
