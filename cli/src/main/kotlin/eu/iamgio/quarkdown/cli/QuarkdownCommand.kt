package eu.iamgio.quarkdown.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.optional
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.StrictPipelineErrorHandler
import java.io.File

/**
 * Main command of the Quarkdown CLI, that processes and executes a Quarkdown source file.
 */
class QuarkdownCommand : CliktCommand() {
    /**
     * Optional Quarkdown source file to process.
     * If not set, the program runs in REPL mode.
     */
    private val source: File? by argument(help = "Source file").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true,
    ).optional()

    /**
     * Optional output directory.
     * If not set, output files are not generated.
     */
    private val outputDirectory: File? by option("-o", "--out", help = "Output directory").file(
        mustExist = false,
        canBeFile = false,
        canBeDir = true,
    )

    /**
     * When enabled, the rendering stage produces pretty output code.
     */
    private val prettyOutput: Boolean by option("--pretty", help = "Pretty output").flag()

    /**
     * When enabled, the rendered code isn't wrapped in a template code.
     * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`, with the content injected in `body`.
     * @see eu.iamgio.quarkdown.rendering.wrapper.RenderWrapper
     */
    private val noWrap: Boolean by option("--nowrap", help = "Don't wrap output").flag()

    /**
     * When enabled, the process is aborted whenever any pipeline error occurs.
     * By default, this is disabled and error messages are displayed in the final document without killing the pipeline.
     */
    private val strict: Boolean by option("--strict", help = "Exit on error").flag()

    override fun run() {
        val cliOptions =
            CliOptions(
                source,
                outputDirectory,
            )

        val pipelineOptions =
            PipelineOptions(
                prettyOutput = prettyOutput,
                wrapOutput = !noWrap,
                workingDirectory = source?.parentFile,
                errorHandler =
                    when {
                        strict -> StrictPipelineErrorHandler()
                        else -> BasePipelineErrorHandler()
                    },
            )

        // Executes the Quarkdown pipeline.
        runQuarkdown(cliOptions, pipelineOptions)
    }
}
