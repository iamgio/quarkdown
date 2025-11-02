package com.quarkdown.cli.exec

import com.quarkdown.core.context.Context
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.output.OutputResource
import java.io.File

/**
 * Outcome of a pipeline execution.
 * @param resource the output resource produced by the pipeline, if any
 * @param directory the directory, child of the configuration's output directory, where the output artifacts are saved.
 *                  If `null`, no output directory was written.
 *                  This can happen in case of errors or, more likely, when running in pipe mode (`--pipe`).
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
