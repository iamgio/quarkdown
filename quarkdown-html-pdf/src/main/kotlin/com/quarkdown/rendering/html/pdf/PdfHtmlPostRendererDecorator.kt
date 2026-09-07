package com.quarkdown.rendering.html.pdf

import com.quarkdown.core.document.sub.getOutputFileName
import com.quarkdown.core.pipeline.error.IOPipelineException
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.visitor.copy
import com.quarkdown.core.pipeline.output.visitor.saveTo
import com.quarkdown.core.rendering.PostRenderer
import com.quarkdown.rendering.html.post.HtmlPostRenderer
import java.io.File

/**
 * Decorator for [HtmlPostRenderer] that generates a PDF file from the HTML output via a Chromium-family browser.
 * @param postRenderer the original [HtmlPostRenderer] to be decorated
 * @param options options for the PDF export process
 */
class PdfHtmlPostRendererDecorator(
    private val postRenderer: HtmlPostRenderer,
    private val options: HtmlPdfExportOptions,
) : PostRenderer by postRenderer {
    override fun generateResources(rendered: CharSequence): Set<OutputResource> {
        val resources = postRenderer.generateResources(rendered)

        val tempDirectory =
            kotlin.io.path
                .createTempDirectory(prefix = "quarkdown-pdf")
                .toFile()

        try {
            val sourcesDirectory: File = OutputResourceGroup("sources", resources).saveTo(tempDirectory)
            val outName = postRenderer.context.subdocument.getOutputFileName(postRenderer.context)
            val out: File = tempDirectory.resolve("$outName.pdf")

            HtmlPdfExporter(options).export(sourcesDirectory, out)

            if (!out.exists()) {
                throw IOPipelineException("PDF export did not produce an output file")
            }

            // In order to comply with the pipeline's contract, the output PDF is wrapped in an OutputResource.
            // It is deleted along with its temporary directory, and will be recreated in the output directory
            // by the pipeline's final process.
            return setOf(BinaryOutputArtifact.fromFile(out))
        } finally {
            tempDirectory.deleteRecursively()
        }
    }

    override fun wrapResources(
        name: String,
        resources: Set<OutputResource>,
    ): OutputResource {
        // Single output file.
        resources.singleOrNull()?.let {
            return it.copy(name = "$name.pdf")
        }
        // Multiple output files (e.g. subdocuments).
        return OutputResourceGroup(
            name = name,
            resources = resources,
        )
    }
}
