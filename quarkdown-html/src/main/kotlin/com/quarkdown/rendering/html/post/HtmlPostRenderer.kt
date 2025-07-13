package com.quarkdown.rendering.html.post

import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.core.ast.attributes.presence.hasMermaidDiagram
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.orDefault
import com.quarkdown.core.media.storage.options.MediaStorageOptions
import com.quarkdown.core.media.storage.options.ReadOnlyMediaStorageOptions
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.rendering.PostRenderer
import com.quarkdown.core.rendering.template.TemplatePlaceholders
import com.quarkdown.core.rendering.withMedia
import com.quarkdown.core.template.TemplateProcessor
import com.quarkdown.rendering.html.css.asCSS
import org.apache.commons.text.StringEscapeUtils

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
 * - KaTeX for math rendering;
 * - HighlightJS for code highlighting.
 * @param baseTemplateProcessor supplier of the base [TemplateProcessor] to inject with content and process
 */
class HtmlPostRenderer(
    private val context: Context,
    private val baseTemplateProcessor: () -> TemplateProcessor = {
        TemplateProcessor.fromResourceName("/render/html-wrapper.html.template")
    },
) : PostRenderer {
    // HTML requires local media to be resolved from the file system.
    override val preferredMediaStorageOptions: MediaStorageOptions =
        ReadOnlyMediaStorageOptions(enableLocalMediaStorage = true)

    override fun createTemplateProcessor() =
        baseTemplateProcessor().apply {
            // Local server port to communicate with.
            optionalValue(
                TemplatePlaceholders.SERVER_PORT,
                context.attachedPipeline
                    ?.options
                    ?.serverPort,
            )
            // Document metadata.
            val document = context.documentInfo
            value(TemplatePlaceholders.TITLE, document.name ?: "Quarkdown")
            optionalValue(TemplatePlaceholders.LANGUAGE, document.locale?.tag)
            value(
                TemplatePlaceholders.DOCUMENT_TYPE,
                document.type.name.lowercase(),
            )
            // "Plain" document rendering.
            conditional(TemplatePlaceholders.IS_PLAIN, document.type == DocumentType.PLAIN)
            // "Paged" document rendering via PagesJS.
            conditional(TemplatePlaceholders.IS_PAGED, document.type == DocumentType.PAGED)
            // "Slides" document rendering via RevealJS.
            conditional(TemplatePlaceholders.IS_SLIDES, document.type == DocumentType.SLIDES)
            // HighlightJS is initialized only if needed.
            conditional(
                TemplatePlaceholders.HAS_CODE,
                context.attributes.hasCode,
            )
            // Mermaid is initialized only if needed.
            conditional(
                TemplatePlaceholders.HAS_MERMAID_DIAGRAM,
                context.attributes.hasMermaidDiagram,
            )
            // KaTeX is initialized only if needed.
            conditional(
                TemplatePlaceholders.HAS_MATH,
                context.attributes.hasMath,
            )
            // Page format.
            val pageFormat = document.layout.pageFormat
            conditional(TemplatePlaceholders.HAS_PAGE_SIZE, pageFormat.hasSize)
            optionalValue(
                TemplatePlaceholders.PAGE_WIDTH,
                pageFormat.pageWidth?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.PAGE_HEIGHT,
                pageFormat.pageHeight?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.PAGE_MARGIN,
                pageFormat.margin?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.FONT_SIZE,
                pageFormat.fontSize?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.PAGE_CONTENT_BORDER_WIDTH,
                pageFormat.contentBorderWidth?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.PAGE_CONTENT_BORDER_COLOR,
                pageFormat.contentBorderColor?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.COLUMN_COUNT,
                pageFormat.columnCount,
            )
            // Alignment can be global or local. See TemplatePlaceholders.GLOBAL_HORIZONTAL_ALIGNMENT for details.
            optionalValue(
                TemplatePlaceholders.GLOBAL_HORIZONTAL_ALIGNMENT,
                pageFormat.alignment?.takeIf { it.isGlobal }?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.LOCAL_HORIZONTAL_ALIGNMENT,
                pageFormat.alignment?.takeIf { it.isLocal }?.asCSS,
            )
            optionalValue(
                TemplatePlaceholders.PARAGRAPH_SPACING,
                document.layout.paragraphStyle.spacing
                    ?.toString()
                    ?.plus("em"),
            )
            optionalValue(
                TemplatePlaceholders.PARAGRAPH_LINE_HEIGHT,
                document.layout.paragraphStyle.lineHeight,
            )
            optionalValue(
                TemplatePlaceholders.PARAGRAPH_LETTER_SPACING,
                document.layout.paragraphStyle.letterSpacing
                    ?.toString()
                    ?.plus("em"),
            )
            optionalValue(
                TemplatePlaceholders.PARAGRAPH_INDENT,
                document.layout.paragraphStyle.indent
                    ?.toString()
                    ?.plus("em"),
            )
            iterable(
                TemplatePlaceholders.TEX_MACROS,
                mapToJsObjectEntries(context.documentInfo.tex.macros),
            )
        }

    private fun sanitizeJs(text: String): String = StringEscapeUtils.escapeEcmaScript(text)

    private fun mapToJsObjectEntries(map: Map<String, String>): List<String> =
        map.map { (key, value) ->
            "\"${sanitizeJs(key)}\": \"${sanitizeJs(value)}\""
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
        }.withMedia(context)

    /**
     * @param theme theme to get the artifacts for
     * @return a set that contains an output artifact for each non-null theme component of [theme]
     *         (e.g. color scheme, layout format, ...)
     */
    private fun retrieveThemeComponentsArtifacts(theme: DocumentTheme?): Set<OutputResource> =
        buildSet {
            fun getFullResourcePath(resourceSubPath: String): String = "/render/theme/$resourceSubPath.css"

            /**
             * Pushes a new output artifact to the set if it exists.
             * @param resourceName name of the resource
             * @param resourcePath path of the resource starting from the theme folder, without extension
             */
            fun artifact(
                resourceName: String,
                resourcePath: String = resourceName,
            ) {
                val artifact =
                    LazyOutputArtifact.internalOrNull(
                        resource = getFullResourcePath(resourcePath),
                        // The name is not used here, as this artifact will be concatenated to others in generateResources.
                        name = resourceName,
                        type = ArtifactType.CSS,
                    )
                if (artifact != null) {
                    this += artifact
                }
            }

            // Pushing theme components.
            artifact("global")
            theme?.layout?.let { artifact(it, "layout/$it") }
            theme?.color?.let { artifact(it, "color/$it") }

            // In case the active locale features its own theme components, add them as well.
            // For example, Chinese typefaces (#105).
            context.documentInfo.locale?.shortTag?.let {
                artifact(it, "locale/$it")
            }

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
            fun artifact(
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

            artifact("script")
            artifact("slides", condition = context.documentInfo.type == DocumentType.SLIDES)
            artifact("paged", condition = context.documentInfo.type == DocumentType.PAGED)
            artifact("math", condition = context.attributes.hasMath)
            artifact("mermaid", condition = context.attributes.hasMermaidDiagram)
            artifact("code", condition = context.attributes.hasCode)
            artifact("websockets", condition = context.attachedPipeline?.options?.useServer == true)
        }

    override fun wrapResources(
        name: String,
        resources: Set<OutputResource>,
    ) = OutputResourceGroup(name, resources)
}
