package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.document.DocumentTheme
import eu.iamgio.quarkdown.document.DocumentType
import eu.iamgio.quarkdown.document.orDefault
import eu.iamgio.quarkdown.media.storage.options.MediaStorageOptions
import eu.iamgio.quarkdown.media.storage.options.ReadOnlyMediaStorageOptions
import eu.iamgio.quarkdown.pipeline.output.ArtifactType
import eu.iamgio.quarkdown.pipeline.output.LazyOutputArtifact
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.pipeline.output.OutputResourceGroup
import eu.iamgio.quarkdown.pipeline.output.TextOutputArtifact
import eu.iamgio.quarkdown.rendering.PostRenderer
import eu.iamgio.quarkdown.rendering.template.TemplatePlaceholders
import eu.iamgio.quarkdown.template.TemplateProcessor

// Default theme components to use if not specified by the user.
private val DEFAULT_THEME =
    DocumentTheme(
        color = "paperwhite",
        layout = "latex",
    )

/**
 * A [PostRenderer] that injects content into an HTML template, which supports out of the box:
 * - RevealJS for slides rendering;
 * - PagedJS for page-based rendering (e.g. books);
 * - MathJax for math rendering;
 * - HighlightJS for code highlighting.
 */
class HtmlPostRenderer(
    private val context: Context,
) : PostRenderer {
    // HTML requires local media to be resolved from the file system.
    override val preferredMediaStorageOptions: MediaStorageOptions =
        ReadOnlyMediaStorageOptions(enableLocalMediaStorage = true)

    override fun createTemplateProcessor() =
        TemplateProcessor.fromResourceName("/render/html-wrapper.html.template").apply {
            // Local server port to communicate with.
            optionalValue(
                TemplatePlaceholders.SERVER_PORT,
                context.attachedPipeline
                    ?.options
                    ?.serverPort,
            )
            // Document metadata.
            value(TemplatePlaceholders.TITLE, context.documentInfo.name ?: "Quarkdown")
            optionalValue(TemplatePlaceholders.LANGUAGE, context.documentInfo.locale?.tag)
            // "Paged" document rendering via PagesJS.
            conditional(TemplatePlaceholders.IS_PAGED, context.documentInfo.type == DocumentType.PAGED)
            // "Slides" document rendering via RevealJS.
            conditional(TemplatePlaceholders.IS_SLIDES, context.documentInfo.type == DocumentType.SLIDES)
            // HighlightJS is initialized only if needed.
            conditional(
                TemplatePlaceholders.HAS_CODE,
                context.attributes.hasCode,
            )
            // MathJax is initialized only if needed.
            conditional(
                TemplatePlaceholders.HAS_MATH,
                context.attributes.hasMath,
            )
            // Page format.
            conditional(TemplatePlaceholders.HAS_PAGE_SIZE, context.documentInfo.pageFormat.hasSize)
            optionalValue(
                TemplatePlaceholders.PAGE_WIDTH,
                context.documentInfo.pageFormat.pageWidth
                    ?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.PAGE_HEIGHT,
                context.documentInfo.pageFormat.pageHeight
                    ?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.PAGE_MARGIN,
                context.documentInfo.pageFormat.margin
                    ?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.COLUMN_COUNT,
                context.documentInfo.pageFormat.columnCount,
            )
            optionalValue(
                TemplatePlaceholders.HORIZONTAL_ALIGNMENT,
                context.documentInfo.pageFormat.alignment
                    ?.asCSS,
            )
        }

    override fun generateResources(rendered: CharSequence): Set<OutputResource> =
        buildSet {
            // The main HTML resource.
            this +=
                TextOutputArtifact(
                    name = "index",
                    content = rendered,
                    type = ArtifactType.HTML,
                )

            // The user-set theme is merged with the default one
            // to fill in the missing components with the default ones.
            val theme = context.documentInfo.theme.orDefault(DEFAULT_THEME)
            // A group of CSS theme resources is added to the output resources.
            // Theme components (global style, color scheme and layout format) are stored in a single group (directory)
            // and linked via @import statements in a theme.css file.
            this +=
                OutputResourceGroup(
                    name = "theme",
                    resources = retrieveThemeComponentsArtifacts(theme),
                )

            // A group of JS script resources is added to the output resources.
            // Only the strictly required scripts are included, depending on the document's characteristics.
            this +=
                OutputResourceGroup(
                    name = "script",
                    resources = retrieveScriptComponentsArtifacts(),
                )
        }

    /**
     * @param theme theme to get the artifacts for
     * @return a set that contains an output artifact for each non-null theme component of [theme]
     *         (e.g. color scheme, layout format, ...)
     */
    private fun retrieveThemeComponentsArtifacts(theme: DocumentTheme?): Set<OutputResource> =
        buildSet {
            /**
             * @param resourceName name of the resource
             * @param resourcePath path of the resource starting from the theme folder, without extension
             * @return a new output artifact from an internal resource
             */
            fun artifact(
                resourceName: String,
                resourcePath: String = resourceName,
            ) = LazyOutputArtifact.internal(
                resource = "/render/theme/$resourcePath.css",
                // The name is not used here, as this artifact will be concatenated to others in generateResources.
                name = resourceName,
                type = ArtifactType.CSS,
            )

            // Pushing theme components.
            this += artifact("global")
            theme?.layout?.let { this += artifact(it, "layout/$it") }
            theme?.color?.let { this += artifact(it, "color/$it") }

            // A theme.css file contains only @import statements for each theme component
            // in order to link them into a single file that can be easily included in the main HTML file.
            this +=
                TextOutputArtifact(
                    name = "theme",
                    content =
                        joinToString(separator = "\n") {
                            "@import url('${it.name}.css');"
                        },
                    type = ArtifactType.CSS,
                )
        }

    /**
     * @return a set that contains an output artifact for each required script component
     */
    private fun retrieveScriptComponentsArtifacts(): Set<OutputResource> =
        buildSet {
            /**
             * Appends a new output artifact to the set if [condition] is true.
             * @param resourceName name of the resource
             * @param resourcePath path of the resource starting from the theme folder, without extension
             */
            fun pushArtifact(
                resourceName: String,
                resourcePath: String = resourceName,
                condition: Boolean = true,
            ) {
                if (!condition) return
                this +=
                    LazyOutputArtifact.internal(
                        resource = "/render/script/$resourcePath.js",
                        // The name is not used here, as this artifact will be concatenated to others in generateResources.
                        name = resourceName,
                        type = ArtifactType.JAVASCRIPT,
                    )
            }

            pushArtifact("script")
            pushArtifact("slides", condition = context.documentInfo.type == DocumentType.SLIDES)
            pushArtifact("paged", condition = context.documentInfo.type == DocumentType.PAGED)
            pushArtifact("math", condition = context.attributes.hasMath)
            pushArtifact("code", condition = context.attributes.hasCode)
            pushArtifact("websockets", condition = context.attachedPipeline?.options?.useServer == true)
        }
}
