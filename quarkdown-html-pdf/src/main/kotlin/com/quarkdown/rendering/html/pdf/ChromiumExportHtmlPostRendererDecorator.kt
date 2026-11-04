package com.quarkdown.rendering.html.pdf

import com.quarkdown.core.document.sub.getOutputFileName
import com.quarkdown.core.pipeline.error.IOPipelineException
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.visitor.copy
import com.quarkdown.core.pipeline.output.visitor.saveTo
import com.quarkdown.core.rendering.PostRenderer
import com.quarkdown.rendering.html.post.HtmlPostRenderer
import java.io.File

/**
 * Decorator for [HtmlPostRenderer] that exports the HTML output to a [ChromiumExportFormat],
 * such as PDF or PNG, via a Chromium-family browser.
 * @param postRenderer the original [HtmlPostRenderer] to be decorated
 * @param options options for the export process
 * @param format format to export the document to
 */
class ChromiumExportHtmlPostRendererDecorator(
    private val postRenderer: HtmlPostRenderer,
    private val options: ChromiumExportOptions,
    private val format: ChromiumExportFormat,
) : PostRenderer by postRenderer {
    override fun generateResources(rendered: CharSequence): Set<OutputResource> {
        val resources = postRenderer.generateResources(rendered)

        val tempDirectory =
            kotlin.io.path
                .createTempDirectory(prefix = "quarkdown-${format.extension}")
                .toFile()

        try {
            val sourcesDirectory: File = OutputResourceGroup("sources", resources).saveTo(tempDirectory)
            val outName = postRenderer.context.subdocument.getOutputFileName(postRenderer.context)
            val out: File = tempDirectory.resolve("$outName.${format.extension}")

            HtmlChromiumExporter(options, format).export(sourcesDirectory, out)

            if (!out.exists()) {
                throw IOPipelineException("${format.displayName} export did not produce an output file")
            }

            // In order to comply with the pipeline's contract, the exported file is wrapped in an OutputResource.
            // It is deleted along with its temporary directory, and will be recreated in the output directory
            // by the pipeline's final process.
            return setOf(
                BinaryOutputArtifact(out.name, out.readBytes().toList(), ArtifactType.AUTO),
            )
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
            return it.copy(name = "$name.${format.extension}")
        }
        // Multiple output files (e.g. subdocuments).
        return OutputResourceGroup(
            name = name,
            resources = resources,
        )
    }
}
