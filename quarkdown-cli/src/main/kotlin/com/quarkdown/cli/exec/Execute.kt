package com.quarkdown.cli.exec

import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.PipelineInitialization
import com.quarkdown.cli.exec.strategy.PipelineExecutionStrategy
import com.quarkdown.cli.lib.QdLibraries
import com.quarkdown.cli.util.cleanDirectory
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.function.error.FunctionCallRuntimeException
import com.quarkdown.core.function.library.LibraryExporter
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.Pipeline
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.error.PipelineException
import com.quarkdown.core.pipeline.output.visitor.saveTo
import kotlin.system.exitProcess

/**
 * Executes a complete Quarkdown pipeline.
 * @param executionStrategy launch strategy of the pipeline, e.g. from file or REPL
 * @param cliOptions options that define the behavior of the CLI, especially I/O
 * @param pipelineOptions options that define the behavior of the pipeline
 * @return the output file or directory, if any, associated with the executed pipeline
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

    try {
        // Cleans the output directory if enabled in options.
        if (cliOptions.clean) {
            outputDirectory?.cleanDirectory()
        }

        // Pipeline execution and output resource retrieving.
        val resource = executionStrategy.execute(pipeline)
        // Exports the generated resources to file if enabled in options.
        val childDirectory = outputDirectory?.let { resource?.saveTo(it) }

        return ExecutionOutcome(resource, childDirectory, pipeline)
    } catch (e: PipelineException) {
        val targetException = (e as? FunctionCallRuntimeException)?.cause ?: e
        targetException.printStackTrace()
        exitProcess(e.code)
    }
}
