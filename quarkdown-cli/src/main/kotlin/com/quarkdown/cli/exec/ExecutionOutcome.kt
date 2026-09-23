package com.quarkdown.cli.exec

import com.quarkdown.core.context.Context
import com.quarkdown.core.pipeline.output.OutputResource
import java.io.File

/**
 * Outcome of a pipeline execution.
 * @param resource the output resource produced by the pipeline, if any
 * @param directory the directory, child of the configuration's output directory, where the output artifacts are saved.
 *                  If `null`, no output directory was written.
 *                  This can happen in case of errors or, more likely, when running in pipe mode (`--pipe`).
 * @param context the context the pipeline ran on, exposing the document information, subdocuments and media it produced.
 *                It is closed, meaning no pipeline is attached to it anymore
 * @see runQuarkdown
 */
data class ExecutionOutcome(
    val resource: OutputResource?,
    val directory: File?,
    val context: Context,
)
