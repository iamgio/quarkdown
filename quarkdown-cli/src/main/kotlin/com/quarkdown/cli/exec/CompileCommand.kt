package com.quarkdown.cli.exec

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.file
import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.exec.strategy.FileExecutionStrategy
import com.quarkdown.cli.server.WebServerOptions
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.server.browser.DefaultBrowserLauncher
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
    ).flag().validate {
        when {
            it && !exportPdf -> Log.warn("--pdf-no-sandbox flag is ignored because --pdf flag is not set.")
            it -> Log.warn("Disabling Chrome sandbox for PDF export. This is potentially unsafe.")
        }
    }

    override fun finalizeCliOptions(original: CliOptions) =
        original.copy(
            source = source,
            noPdfSandbox = noPdfSandbox,
            exportPdf = exportPdf,
        )

    override fun createExecutionStrategy(cliOptions: CliOptions) = FileExecutionStrategy(source)

    override fun postExecute(
        outcome: ExecutionOutcome,
        cliOptions: CliOptions,
        pipelineOptions: PipelineOptions,
    ) {
        if (outcome.directory == null) {
            Log.warn("Unexpected null output directory during compilation post-processing")
            return
        }

        if (super.preview) {
            launchServer(outcome.directory)
        }
    }

    private fun launchServer(directory: File) {
        // Communicates with the server to reload the requested resources.
        // If enabled and the server is not running, also starts the server
        // (this is shorthand for `quarkdown start -f <generated directory> -p <server port> -o`).
        runServerCommunication(
            startServerOnFailedConnection = true,
            WebServerOptions(
                port = super.serverPort,
                targetFile = directory,
                browserLauncher = DefaultBrowserLauncher(),
            ),
        )
    }
}
