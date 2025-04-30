package com.quarkdown.cli.exec

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import com.quarkdown.cli.CliOptions
import com.quarkdown.cli.exec.strategy.FileExecutionStrategy
import com.quarkdown.cli.server.WebServerOptions
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.pdf.PdfExportOptions
import com.quarkdown.pdf.PdfExporters
import com.quarkdown.server.browser.DefaultBrowserLauncher
import java.io.File
import kotlin.concurrent.thread

/**
 * Command to compile a Quarkdown file into an output.
 * @see FileExecutionStrategy
 */
class CompileCommand : ExecuteCommand("c") {
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
    ).flag()

    override fun finalizeCliOptions(original: CliOptions) = original.copy(source = source)

    override fun createExecutionStrategy(cliOptions: CliOptions) = FileExecutionStrategy(source)

    /**
     * Runs the action in parallel if needed.
     */
    private fun runParallelizable(action: () -> Unit) {
        val parallelizeCondition = super.preview
        when {
            parallelizeCondition -> thread { action() }
            else -> action()
        }
    }

    override fun postExecute(
        outcome: ExecutionOutcome,
        cliOptions: CliOptions,
        pipelineOptions: PipelineOptions,
    ) {
        if (outcome.directory == null) {
            Log.warn("Unexpected null output directory during compilation post-processing")
            return
        }

        if (noPdfSandbox) {
            when {
                !exportPdf -> Log.warn("--pdf-no-sandbox flag is ignored because --pdf flag is not set.")
                else -> Log.warn("Disabling Chrome sandbox for PDF export. This is potentially unsafe.")
            }
        }

        if (exportPdf) {
            runParallelizable { exportPdf(outcome.directory) }
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

    private fun exportPdf(
        sourceDirectory: File,
        outDirectory: File = sourceDirectory.parentFile,
    ) {
        val out = File(outDirectory, "${sourceDirectory.name}.pdf")

        // Currently, HTML is hardcoded. In the future, more targets can be chosen
        // and the PDF exporter for the corresponding target should be selected.
        val html = QuarkdownFlavor.rendererFactory.html(MutableContext(QuarkdownFlavor))

        val options = PdfExportOptions(super.nodePath, super.npmPath, this.noPdfSandbox)
        val exporter = PdfExporters.getForRenderingTarget(html.postRenderer, options)

        try {
            exporter.export(sourceDirectory, out)
        } catch (e: Exception) {
            Log.error("Failed to export PDF: ${e.message}")
        }
    }
}
