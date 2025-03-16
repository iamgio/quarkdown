package eu.iamgio.quarkdown.cli.exec

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import eu.iamgio.quarkdown.cli.CliOptions
import eu.iamgio.quarkdown.cli.exec.strategy.FileExecutionStrategy
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.pdf.PdfExportOptions
import eu.iamgio.quarkdown.pdf.PdfExporters
import eu.iamgio.quarkdown.pipeline.PipelineOptions
import java.io.File

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

    override fun finalizeCliOptions(original: CliOptions) = original.copy(source = source)

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

        if (exportPdf) {
            exportPdf(outcome.directory)
        }
    }

    private fun exportPdf(
        sourceDirectory: File,
        outDirectory: File = sourceDirectory.parentFile,
    ) {
        val out = File(outDirectory, "${sourceDirectory.name}.pdf")

        // Currently, HTML is hardcoded. In the future, more targets can be chosen
        // and the PDF exporter for the corresponding target should be selected.
        val html = QuarkdownFlavor.rendererFactory.html(MutableContext(QuarkdownFlavor))

        val options = PdfExportOptions(super.nodePath, super.npmPath)
        val exporter = PdfExporters.getForRenderingTarget(html.postRenderer, options)
        exporter.export(sourceDirectory, out)
    }
}
