package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.cli.exec.FileExecutionStrategy
import eu.iamgio.quarkdown.cli.exec.PipelineExecutionStrategy
import eu.iamgio.quarkdown.cli.exec.ReplExecutionStrategy
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pipeline.Pipeline
import eu.iamgio.quarkdown.pipeline.error.PipelineException
import eu.iamgio.quarkdown.pipeline.options.PipelineOptions
import eu.iamgio.quarkdown.pipeline.output.FileResourceExporter
import java.io.File
import kotlin.system.exitProcess

/**
 * Executes a complete Quarkdown pipeline.
 * @param source main source file to process. If not set, the program runs in REPL mode
 * @param options pipeline options that define the behavior of the pipeline
 */
fun runQuarkdown(
    source: File?,
    options: PipelineOptions,
) {
    // Flavor to use across the pipeline.
    val flavor: MarkdownFlavor = QuarkdownFlavor

    // The pipeline that contains all the stages to go through,
    // from the source input to the final output.
    val pipeline: Pipeline = PipelineInitialization.init(flavor, options)

    // Type of execution to launch.
    // If a source file is set, execute the content of a single file.
    // Otherwise, run in REPL mode. Note: context is shared across executions.
    val execution: PipelineExecutionStrategy = source?.let(::FileExecutionStrategy) ?: ReplExecutionStrategy()

    try {
        // Pipeline execution and output resource retrieving.
        val resource = execution.execute(pipeline)

        // Exports the generated resources to file if enabled by system properties.
        pipeline.options.outputDirectory?.let { directory ->
            resource?.accept(FileResourceExporter(location = directory))
        }
    } catch (e: PipelineException) {
        e.printStackTrace()
        exitProcess(e.code)
    }
}

fun main(args: Array<String>) = QuarkdownCommand().main(args)
