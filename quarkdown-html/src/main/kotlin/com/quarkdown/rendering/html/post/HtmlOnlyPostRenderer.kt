package com.quarkdown.rendering.html.post

import com.quarkdown.core.context.Context
import com.quarkdown.core.media.storage.options.MediaStorageOptions
import com.quarkdown.core.media.storage.options.ReadOnlyMediaStorageOptions
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.rendering.PostRenderer
import com.quarkdown.core.template.TemplateProcessor

/**
 * A subset of [HtmlPostRenderer] that generates only the HTML output resources without any additional resources.
 *
 * It supports out of the box:
 * - RevealJS for slides rendering;
 * - PagedJS for page-based rendering (e.g. books);
 * - KaTeX for math rendering;
 * - HighlightJS for code highlighting.
 *
 * @param name the name of the HTML output resource, without extension
 * @param context the rendering context
 * @param baseTemplateProcessor supplier of the base [TemplateProcessor] to inject with content and process via [HtmlPostRendererTemplate].
 */
class HtmlOnlyPostRenderer(
    private val name: String,
    private val context: Context,
    private val baseTemplateProcessor: () -> TemplateProcessor = baseHtmlTemplateProcessor,
) : PostRenderer {
    // HTML requires local media to be resolved from the file system.
    override val preferredMediaStorageOptions: MediaStorageOptions =
        ReadOnlyMediaStorageOptions(enableLocalMediaStorage = true)

    override fun createTemplateProcessor() =
        HtmlPostRendererTemplate(
            baseTemplateProcessor(),
            context,
        ).create()

    override fun generateResources(rendered: CharSequence): Set<OutputResource> =
        buildSet {
            // The main HTML resource.
            this +=
                TextOutputArtifact(
                    name = name,
                    content = rendered,
                    type = ArtifactType.HTML,
                )
        }

    override fun wrapResources(
        name: String,
        resources: Set<OutputResource>,
    ) = resources.first()
}
