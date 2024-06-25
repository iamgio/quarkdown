package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.cli.exec.FileExecutionStrategy
import eu.iamgio.quarkdown.cli.exec.PipelineExecutionStrategy
import eu.iamgio.quarkdown.cli.exec.ReplExecutionStrategy
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pipeline.error.PipelineException
import eu.iamgio.quarkdown.pipeline.options.MutablePipelineOptions
import eu.iamgio.quarkdown.pipeline.output.FileResourceExporter
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    // Flavor to use across the pipeline.
    val flavor: MarkdownFlavor = QuarkdownFlavor

    // Settings that affect different behaviors of the pipeline.
    val options = MutablePipelineOptions(exitOnError = true)

    // The pipeline that contains all the stages to go through,
    // from the source input to the final output.
    val pipeline = PipelineInitialization.init(flavor, options)

    // Type of execution to launch.
    val execution: PipelineExecutionStrategy =
        if (args.isNotEmpty()) {
            // Execute the content of a single file.
            FileExecutionStrategy(File(args.first()))
        } else {
            // Run in REPL mode.
            // Note: context is shared across executions.
            ReplExecutionStrategy()
        }

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
