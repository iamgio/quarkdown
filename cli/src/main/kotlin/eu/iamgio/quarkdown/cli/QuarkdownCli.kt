package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.cli.exec.FileExecutionStrategy
import eu.iamgio.quarkdown.cli.exec.PipelineExecutionStrategy
import eu.iamgio.quarkdown.cli.exec.ReplExecutionStrategy
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.pipeline.error.PipelineException
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    // Flavor to use across the pipeline.
    val flavor: MarkdownFlavor = QuarkdownFlavor

    // The pipeline that contains all the stages to go through,
    // from the source input to the final output.
    val pipeline = PipelineInitialization.init(flavor)

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
        execution.execute(pipeline)
    } catch (e: PipelineException) {
        e.printStackTrace()
        exitProcess(e.code)
    }
}
