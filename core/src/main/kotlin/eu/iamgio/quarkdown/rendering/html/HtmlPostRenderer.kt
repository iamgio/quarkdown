package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.pipeline.output.ArtifactType
import eu.iamgio.quarkdown.pipeline.output.LazyOutputArtifact
import eu.iamgio.quarkdown.pipeline.output.OutputArtifact
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.rendering.PostRenderer
import eu.iamgio.quarkdown.rendering.wrapper.RenderWrapper
import eu.iamgio.quarkdown.rendering.wrapper.TemplatePlaceholders

/**
 * A [PostRenderer] that injects content into an HTML template, which supports out of the box:
 * - PagedJS for page-based rendering (e.g. books) with page customization;
 * - MathJax for math rendering;
 * - HighlightJS for code highlighting.
 */
class HtmlPostRenderer(private val context: Context) : PostRenderer {
    override fun createCodeWrapper() =
        RenderWrapper.fromResourceName("/render/html-wrapper.html")
            .value(TemplatePlaceholders.TITLE, context.documentInfo.name ?: "Quarkdown")
            .value(TemplatePlaceholders.LANGUAGE, "en") // TODO set language
            // "Paged" document rendering via PagesJS.
            .conditional(TemplatePlaceholders.IS_PAGED, context.documentInfo.type == DocumentType.PAGED)
            // "Slides" document rendering via RevealJS.
            .conditional(TemplatePlaceholders.IS_SLIDES, context.documentInfo.type == DocumentType.SLIDES)
            .conditional(TemplatePlaceholders.HAS_CODE, context.hasCode) // HighlightJS is initialized only if needed.
            .conditional(TemplatePlaceholders.HAS_MATH, context.hasMath) // MathJax is initialized only if needed.
            .conditional(
                TemplatePlaceholders.HAS_THEME,
                context.documentInfo.theme != null,
            ) // The theme CSS is applied only if a theme is set.
            // Page format
            .conditional(TemplatePlaceholders.HAS_PAGE_SIZE, context.documentInfo.pageFormat.hasSize)
            .value(TemplatePlaceholders.PAGE_WIDTH, context.documentInfo.pageFormat.pageWidth.toString())
            .value(TemplatePlaceholders.PAGE_HEIGHT, context.documentInfo.pageFormat.pageHeight.toString())
            .optionalValue(TemplatePlaceholders.PAGE_MARGIN, context.documentInfo.pageFormat.margin?.asCSS)

    override fun generateResources(rendered: CharSequence): Set<OutputResource> =
        buildSet {
            // The main HTML resource.
            this +=
                OutputArtifact(
                    name = context.documentInfo.name ?: "index",
                    content = rendered,
                    type = ArtifactType.HTML,
                )

            // A CSS theme resource is added to the output resources if a theme is set.
            context.documentInfo.theme?.let {
                this +=
                    LazyOutputArtifact.internal(
                        resource = "/render/theme/$it.css",
                        name = "theme",
                        type = ArtifactType.CSS,
                    )
            }

            if (context.documentInfo.type == DocumentType.SLIDES) {
                this +=
                    LazyOutputArtifact.internal(
                        resource = "/render/script/slides.js",
                        name = "slides",
                        type = ArtifactType.JS,
                    )
            }
        }
}
