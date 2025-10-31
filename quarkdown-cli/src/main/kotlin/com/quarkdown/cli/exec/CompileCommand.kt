package com.quarkdown.cli.exec

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.exec.strategy.FileExecutionStrategy
import com.quarkdown.cli.server.WebServerOptions
import com.quarkdown.cli.server.browserLauncherOption
import com.quarkdown.cli.util.MillisStopwatch
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.interaction.Env
import com.quarkdown.server.ServerEndpoints
import com.quarkdown.server.browser.BrowserLauncher
import com.quarkdown.server.browser.DefaultBrowserLauncher
import com.quarkdown.server.message.ServerMessageSession
import java.io.File

/**
 * Command to compile a Quarkdown file into an output.
 * @see FileExecutionStrategy
 */
class CompileCommand : ExecuteCommand("compile") {
    /**
     * Quarkdown source file to process.
     */
    private val source: File by argument(help = "Source file").file(
        mustExist = true,
        canBeDir = false,
        mustBeReadable = true,
    )

    /**
     * Whether to export to PDF.
     */
    private val exportPdf: Boolean by option("--pdf", help = "Export to PDF").flag()

    /**
     * Whether to disable Chrome sandbox for PDF export from HTML. Potentially unsafe.
     */
    private val noPdfSandbox: Boolean by option(
        "--pdf-no-sandbox",
        help = "(Unsafe) Disable Chrome sandbox for PDF export",
        envvar = Env.NO_SANDBOX,
    ).flag()

    /**
     * When enabled, the rendered content (NOT post-rendered) is printed to stdout and nothing else is logged,
     * suitable for piping the output to other commands.
     */
    private val pipe: Boolean by option("--pipe", help = "Print only the rendered content to stdout").flag()

    /**
     * Optional browser to open the served file in, if preview is enabled.
     */
    private val browser: BrowserLauncher? by browserLauncherOption(
        default = DefaultBrowserLauncher(),
        shouldValidate = { preview },
    )

    /**
     * Session to communicate with the server in order to trigger reloads of the preview.
     */
    private val reloadSession: ServerMessageSession by lazy {
        ServerMessageSession(
            port = super.serverPort,
            endpoint = ServerEndpoints.RELOAD_LIVE_PREVIEW,
        )
    }

    /**
     * Finalizes the CLI options before execution.
     * - Sets the source file
     * - Disables file output when in pipe mode
     * - Sets PDF export options
     */
    override fun finalizeCliOptions(original: CliOptions) =
        original.copy(
            source = source,
            outputDirectory = original.outputDirectory.takeUnless { pipe },
            pipe = pipe,
            exportPdf = exportPdf,
            noPdfSandbox = noPdfSandbox,
        )

    /**
     * Stopwatch to measure the duration of the compilation.
     */
    @get:Synchronized @set:Synchronized
    private lateinit var stopwatch: MillisStopwatch

    override fun createExecutionStrategy(cliOptions: CliOptions) = FileExecutionStrategy(source)

    override fun preExecute(
        cliOptions: CliOptions,
        pipelineOptions: PipelineOptions,
    ) {
        this.stopwatch = MillisStopwatch()
    }

    private fun logCompletion(output: File) {
        if (super.preview && this::stopwatch.isInitialized) {
            val elapsed = stopwatch.elapsedMillis()
            Log.success("in ${elapsed}ms")
        } else {
            Log.success("@ ${output.absolutePath}")
        }
    }

    override fun postExecute(
        outcome: ExecutionOutcome,
        cliOptions: CliOptions,
        pipelineOptions: PipelineOptions,
    ) {
        if (cliOptions.pipe) {
            // No action needed when in pipe mode.
            return
        }

        if (outcome.directory == null) {
            Log.warn("Unexpected null output directory during compilation post-processing")
            return
        }

        this.logCompletion(output = outcome.directory)

        if (super.preview) {
            runServerCommunication(outcome.directory)
        }
    }

    private fun runServerCommunication(directory: File) {
        // Communicates with the server to reload the requested resources.
        // If enabled and the server is not running, also starts the server
        // (this is shorthand for `quarkdown start -f <generated directory> -p <server port> -b default`).
        runServerCommunication(
            WebServerOptions(
                port = super.serverPort,
                targetFile = directory,
                browserLauncher = browser,
                preferLivePreviewUrl = super.preview && super.watch,
            ),
            reloadSession,
        )
    }
}
