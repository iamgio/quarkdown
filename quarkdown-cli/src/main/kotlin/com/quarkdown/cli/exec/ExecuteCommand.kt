package com.quarkdown.cli.exec

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.exec.strategy.PipelineExecutionStrategy
import com.quarkdown.cli.server.DEFAULT_SERVER_PORT
import com.quarkdown.cli.util.thisExecutableFile
import com.quarkdown.cli.watcher.DirectoryWatcher
import com.quarkdown.core.log.Log
import com.quarkdown.core.media.storage.options.ReadOnlyMediaStorageOptions
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.core.pipeline.error.StrictPipelineErrorHandler
import com.quarkdown.interaction.executable.NodeJsWrapper
import com.quarkdown.interaction.executable.NpmWrapper
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
val DEFAULT_LIBRARY_DIRECTORY = ".." + File.separator + "lib" + File.separator + "qmd"

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
    private val outputDirectory: File? by option("-o", "--out", help = "Output directory")
        .file(
            mustExist = false,
            canBeFile = false,
            canBeDir = true,
        ).default(File(DEFAULT_OUTPUT_DIRECTORY))

    /**
     * Optional library directory.
     * If not set, the program looks for libraries in [DEFAULT_LIBRARY_DIRECTORY], relative to the executable JAR file location.
     */
    private val libraryDirectory: File? by option("-l", "--libs", help = "Library directory")
        .file(
            mustExist = true,
            canBeFile = false,
            canBeDir = true,
        ).default(File(thisExecutableFile?.parentFile, DEFAULT_LIBRARY_DIRECTORY))

    /**
     * The rendering target to generate output for.
     */
    private val renderer: String by option(
        "-r",
        "--render",
        help = "Rendering target to generate output for",
    ).default("html")

    /**
     * When enabled, the rendering stage produces pretty output code.
     */
    private val prettyOutput: Boolean by option("--pretty", help = "Pretty output").flag()

    /**
     * When enabled, the rendered code isn't wrapped in a template code.
     * For example, an HTML wrapper may add `<html><head>...</head><body>...</body></html>`, with the content injected in `body`.
     * @see com.quarkdown.core.template.TemplateProcessor
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
     * When enabled, the program communicates with the local server to dynamically reload the requested resources.
     */
    protected val preview: Boolean by option("-p", "--preview", help = "Open or reload content after compiling").flag()

    /**
     * When enabled, the program watches for file changes and automatically recompiles the source.
     * If [preview] is enabled as well, this allows for live reloading.
     */
    private val watch: Boolean by option("-w", "--watch", help = "Watch for file changes").flag()

    /**
     * Port to communicate with the local server on if [preview] is enabled.
     */
    protected val serverPort: Int by option("--server-port", help = "Port to communicate with the local server on")
        .int()
        .default(DEFAULT_SERVER_PORT)

    /**
     * Path to the Node.js executable, needed for PDF export.
     */
    private val nodePath: String by option("--node-path", help = "Path to the Node.js executable")
        .default(NodeJsWrapper.defaultPath)

    /**
     * Path to the npm executable, needed for PDF export.
     */
    protected val npmPath: String by option("--npm-path", help = "Path to the npm executable")
        .default(NpmWrapper.defaultPath)

    /**
     * @return the finalized CLI options based on the command's properties
     */
    fun createCliOptions() =
        CliOptions(
            // Might be overridden by a subclass via `finalizeCliOptions`, e.g. `CompileCommand` which requires a source file.
            source = null,
            outputDirectory,
            libraryDirectory,
            renderer,
            clean,
            nodePath,
            npmPath,
        ).let(::finalizeCliOptions)

    /**
     * @param cliOptions finalized CLI options
     * @return pipeline options based on the command's properties
     */
    fun createPipelineOptions(cliOptions: CliOptions) =
        PipelineOptions(
            prettyOutput = prettyOutput,
            wrapOutput = !noWrap,
            workingDirectory = cliOptions.source?.absoluteFile?.parentFile,
            enableMediaStorage = !noMediaStorage,
            serverPort = serverPort.takeIf { preview },
            mediaStorageOptionsOverrides = ReadOnlyMediaStorageOptions(),
            errorHandler =
                when {
                    strict -> StrictPipelineErrorHandler()
                    else -> BasePipelineErrorHandler()
                },
        )

    override fun run() {
        val cliOptions = this.createCliOptions()
        val pipelineOptions = this.createPipelineOptions(cliOptions)

        // If file watching is enabled, a file change triggers the pipeline execution again.
        cliOptions.takeIf { watch }?.source?.absoluteFile?.parentFile?.let { sourceDirectory ->
            Log.info("Watching for file changes in source directory: $sourceDirectory")

            DirectoryWatcher
                .create(sourceDirectory, exclude = cliOptions.outputDirectory) { event ->
                    Log.info("File changed: ${event.path()}. Launching.")
                    execute(cliOptions, pipelineOptions)
                }.watch()
        }

        // Executes the Quarkdown pipeline.
        execute(cliOptions, pipelineOptions)
    }

    /**
     * Executes the Quarkdown pipeline: compiles and generates output files.
     * If enabled, it also sends a message to the webserver.
     */
    private fun execute(
        cliOptions: CliOptions,
        pipelineOptions: PipelineOptions,
    ) {
        // Executes the Quarkdown pipeline.
        val outcome: ExecutionOutcome = runQuarkdown(createExecutionStrategy(cliOptions), cliOptions, pipelineOptions)

        this.postExecute(outcome, cliOptions, pipelineOptions)
    }

    /**
     * Executes actions after the execution of the pipeline has been completed
     * and the output files have been generated.
     */
    protected open fun postExecute(
        outcome: ExecutionOutcome,
        cliOptions: CliOptions,
        pipelineOptions: PipelineOptions,
    ) {
    }
}
