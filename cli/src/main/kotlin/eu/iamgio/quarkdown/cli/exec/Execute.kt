package eu.iamgio.quarkdown.cli.exec

import eu.iamgio.quarkdown.cli.CliOptions
import eu.iamgio.quarkdown.cli.PipelineInitialization
import eu.iamgio.quarkdown.cli.exec.strategy.PipelineExecutionStrategy
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
import eu.iamgio.quarkdown.server.message.Reload
import eu.iamgio.quarkdown.server.message.ServerMessage
import kotlin.system.exitProcess

/**
 * Executes a complete Quarkdown pipeline.
 * @param executionStrategy launch strategy of the pipeline, e.g. from file or REPL
 * @param cliOptions options that define the behavior of the CLI, especially I/O
 * @param pipelineOptions options that define the behavior of the pipeline
 */
fun runQuarkdown(
    executionStrategy: PipelineExecutionStrategy,
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

    // Output directory to save the generated resources in.
    val directory = cliOptions.outputDirectory

    try {
        // Cleans the output directory if enabled in options.
        if (cliOptions.clean) {
            directory?.cleanDirectory()
        }

        // Pipeline execution and output resource retrieving.
        val resource = executionStrategy.execute(pipeline)

        // Exports the generated resources to file if enabled in options.
        directory?.let { resource?.saveTo(it) }
    } catch (e: PipelineException) {
        e.printStackTrace()
        exitProcess(e.code)
    }

    // If enabled, communicates with the server to reload the requested resources, for instance in the browser.
    cliOptions.serverPort?.let {
        try {
            ServerMessage(Reload).send(port = it)
        } catch (e: Exception) {
            Log.error("Could not communicate with the server on port $it: ${e.message}")
            Log.debug(e)
        }
    }
}
