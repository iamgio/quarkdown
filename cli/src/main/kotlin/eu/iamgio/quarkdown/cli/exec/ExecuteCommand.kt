package eu.iamgio.quarkdown.cli.exec

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import eu.iamgio.quarkdown.cli.CliOptions
import eu.iamgio.quarkdown.cli.DEFAULT_SERVER_PORT
import eu.iamgio.quarkdown.cli.exec.strategy.PipelineExecutionStrategy
import eu.iamgio.quarkdown.cli.util.thisExecutableFile
import eu.iamgio.quarkdown.media.storage.options.ReadOnlyMediaStorageOptions
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import eu.iamgio.quarkdown.pipeline.error.BasePipelineErrorHandler
import eu.iamgio.quarkdown.pipeline.error.StrictPipelineErrorHandler
import java.io.File

/**
 * Name of the default directory to save output files in.
 * It can be overridden by the user.
 */
const val DEFAULT_OUTPUT_DIRECTORY = "output"

/**
 * Name of the default directory to load libraries from.
 * The default value is relative to the executable JAR file location, and points to the `lib/qmd` directory of the distribution archive.
 * It can be overridden by the user.
 */
val DEFAULT_LIBRARY_DIRECTORY = "" + File.separator + "lib" + File.separator + "qmd"

/**
 * Template for Quarkdown commands that launch a complete pipeline and produce output files.
 * @param name name of the command
 * @see CompileCommand
 * @see ReplCommand
 */
abstract class ExecuteCommand(
    name: String,
) : CliktCommand(name) {
    /**
     * @param cliOptions options that define the behavior of the CLI (already finalized by [finalizeCliOptions])
     * @return strategy to launch the pipeline, e.g. from file or REPL
     */
    protected abstract fun createExecutionStrategy(cliOptions: CliOptions): PipelineExecutionStrategy

    /**
     * Finalizes the CLI options before running the pipeline by creating a new instance.
     * The [original] options are created by [ExecuteCommand]'s (= this base class) properties.
     * @param original original CLI options
     * @return finalized CLI options
     */
    protected open fun finalizeCliOptions(original: CliOptions): CliOptions = original

    /**
     * Optional output directory.
     * If not set, the output is saved in [DEFAULT_OUTPUT_DIRECTORY].
     */
    private val outputDirectory: File? by option("-o", "--out", help = "Output directory").file(
        mustExist = false,
        canBeFile = false,
        canBeDir = true,
    ).default(File(DEFAULT_OUTPUT_DIRECTORY))

    /**
     * Optional library directory.
     * If not set, the program looks for libraries in [DEFAULT_LIBRARY_DIRECTORY], relative to the executable JAR file location.
     */
    private val libraryDirectory: File? by option("-l", "--libs", help = "Library directory").file(
        mustExist = true,
        canBeFile = false,
        canBeDir = true,
    ).default(File(thisExecutableFile?.parentFile, DEFAULT_LIBRARY_DIRECTORY))

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

    /**
     * When enabled, the output directory is cleaned before generating new files.
     */
    private val clean: Boolean by option("--clean", help = "Clean output directory").flag()

    /**
     * When enabled, the program does not store any media (e.g. images) into the output directory `media` directory
     * and nodes that reference those media objects are not updated to reflect the new local path.
     */
    private val noMediaStorage: Boolean by option("--no-media-storage", help = "Disables media storage").flag()

    /**
     * When enabled, the program communicates with the local server, for instance to dynamically reload the requested resources.
     */
    private val useServer: Boolean by option("-s", "--use-server", help = "Enable communication with the local server").flag()

    /**
     * Port to communicate with the local server on if [useServer] is enabled.
     */
    private val serverPort: Int by option("--server-port", help = "Port to communicate with the local server on").int()
        .default(DEFAULT_SERVER_PORT)

    override fun run() {
        val cliOptions =
            CliOptions(
                // Might be overridden by a subclass via `finalizeCliOptions`, e.g. `CompileCommand` which requires a source file.
                source = null,
                outputDirectory,
                libraryDirectory,
                clean,
            ).let(::finalizeCliOptions)

        val pipelineOptions =
            PipelineOptions(
                prettyOutput = prettyOutput,
                wrapOutput = !noWrap,
                workingDirectory = cliOptions.source?.parentFile,
                enableMediaStorage = !noMediaStorage,
                serverPort = serverPort.takeIf { useServer },
                mediaStorageOptionsOverrides = ReadOnlyMediaStorageOptions(),
                errorHandler =
                    when {
                        strict -> StrictPipelineErrorHandler()
                        else -> BasePipelineErrorHandler()
                    },
            )

        // Executes the Quarkdown pipeline.
        runQuarkdown(createExecutionStrategy(cliOptions), cliOptions, pipelineOptions)
    }
}
