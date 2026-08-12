package com.quarkdown.cli.exec

import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.PipelineInitialization
import com.quarkdown.cli.exec.strategy.PipelineExecutionStrategy
import com.quarkdown.cli.lib.QdLibraries
import com.quarkdown.cli.util.cleanDirectory
import com.quarkdown.cli.util.runWithTimeout
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.error.PipelineException
import com.quarkdown.core.pipeline.output.visitor.saveTo

/**
 * Executes a complete Quarkdown pipeline.
 *
 * This is the entry point used by both the CLI and any in-process embedder.
 * On any [PipelineException] the exception propagates to the caller, for embedders to catch and handle.
 * If [CliOptions.timeoutSeconds] is set and expires, an [ExecutionTimeoutException] is raised instead.
 *
 * @param executionStrategy launch strategy of the pipeline, e.g. from file or REPL
 * @param cliOptions options that define the behavior of the CLI, especially I/O and the execution timeout
 * @param pipelineOptions options that define the behavior of the pipeline
 * @return the outcome of the executed pipeline, carrying the produced resource and directory (if any)
 * @throws PipelineException if the pipeline fails and its error handler rethrows the error
 * @throws ExecutionTimeoutException if [CliOptions.timeoutSeconds] is set and the execution exceeds it
 */
fun runQuarkdown(
    executionStrategy: PipelineExecutionStrategy,
    cliOptions: CliOptions,
    pipelineOptions: PipelineOptions,
): ExecutionOutcome {
    // Flavor to use across the pipeline.
    val flavor: MarkdownFlavor = QuarkdownFlavor

    // External libraries loaded from .qd files.
    val libraries: Set<LibraryExporter> =
        try {
            cliOptions.libraryDirectory?.let(QdLibraries::fromDirectory) ?: emptySet()
        } catch (e: Exception) {
            Log.warn(e.message ?: "")
            emptySet()
        }

    // The pipeline that contains all the stages to go through,
    // from the source input to the final output.
    val pipeline: Pipeline =
        PipelineInitialization.init(
            flavor,
            libraries,
            pipelineOptions,
            printOutput = cliOptions.pipe,
            cliOptions.renderer,
        )

    // Output directory to save the generated resources in.
    val outputDirectory = cliOptions.outputDirectory

    // Cleans the output directory if enabled in options.
    if (cliOptions.clean) {
        outputDirectory?.cleanDirectory()
    }

    // Pipeline execution.
    return runWithTimeout(cliOptions.timeoutSeconds) {
        val resource = executionStrategy.execute(pipeline)
        val childDirectory = outputDirectory?.let { resource?.saveTo(it) }
        ExecutionOutcome(resource, childDirectory, pipeline)
    }
}
