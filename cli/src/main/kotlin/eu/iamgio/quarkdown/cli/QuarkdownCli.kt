package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.cli.exec.FileExecutionStrategy
import eu.iamgio.quarkdown.cli.exec.PipelineExecutionStrategy
import eu.iamgio.quarkdown.cli.exec.ReplExecutionStrategy
import eu.iamgio.quarkdown.cli.lib.QmdLibraries
import eu.iamgio.quarkdown.cli.util.cleanDirectory
import eu.iamgio.quarkdown.cli.util.saveTo
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.function.library.LibraryExporter
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import eu.iamgio.quarkdown.pipeline.error.PipelineException
import kotlin.system.exitProcess

/**
 * Executes a complete Quarkdown pipeline.
 * @param cliOptions options that define the behavior of the CLI, especially I/O
 * @param pipelineOptions options that define the behavior of the pipeline
 */
fun runQuarkdown(
    cliOptions: CliOptions,
    pipelineOptions: PipelineOptions,
) {
    // Flavor to use across the pipeline.
    val flavor: MarkdownFlavor = QuarkdownFlavor

    // External libraries loaded from .qmd files.
    val libraries: Set<LibraryExporter> =
        try {
            cliOptions.libraryDirectory?.let(QmdLibraries::fromDirectory) ?: emptySet()
        } catch (e: Exception) {
            Log.warn(e.message ?: "")
            emptySet()
        }

    // The pipeline that contains all the stages to go through,
    // from the source input to the final output.
    val pipeline: Pipeline = PipelineInitialization.init(flavor, libraries, pipelineOptions)

    // Type of execution to launch.
    // If a source file is set, execute the content of a single file.
    // Otherwise, run in REPL mode. Note: context is shared across executions.
    val execution: PipelineExecutionStrategy =
        cliOptions.source?.let(::FileExecutionStrategy) ?: ReplExecutionStrategy()

    // Output directory to save the generated resources in.
    val directory = cliOptions.outputDirectory

    try {
        // Cleans the output directory if enabled in options.
        if (cliOptions.clean) {
            directory?.cleanDirectory()
        }

        // Pipeline execution and output resource retrieving.
        val resource = execution.execute(pipeline)

        // Exports the generated resources to file if enabled in options.
        directory?.let { resource?.saveTo(it) }
    } catch (e: PipelineException) {
        e.printStackTrace()
        exitProcess(e.code)
    }
}

fun main(args: Array<String>) = QuarkdownCommand().main(args)
