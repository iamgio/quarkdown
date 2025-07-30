package com.quarkdown.rendering.html.post

import com.quarkdown.core.ast.attributes.presence.hasCode
import com.quarkdown.core.ast.attributes.presence.hasMath
import com.quarkdown.core.ast.attributes.presence.hasMermaidDiagram
import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.document.DocumentType
import com.quarkdown.core.document.orDefault
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.rendering.PostRenderer
import com.quarkdown.core.rendering.withMedia
import com.quarkdown.core.template.TemplateProcessor

// Default theme components to use if not specified by the user.
private val DEFAULT_THEME =
    DocumentTheme(
        color = "paperwhite",
        layout = "latex",
    )

/**
 * A [PostRenderer] that injects content into an HTML template. This includes all the features of [HtmlOnlyPostRenderer], plus:
 * - Theme components
 * - Runtime scripts
 * - Media resources
 *
 * @param baseTemplateProcessor supplier of the base [TemplateProcessor] to inject with content and process via [HtmlPostRendererTemplate].
 */
class HtmlPostRenderer(
    val context: Context,
    private val baseTemplateProcessor: () -> TemplateProcessor = baseHtmlTemplateProcessor,
    private val base: HtmlOnlyPostRenderer = HtmlOnlyPostRenderer(name = "index", context, baseTemplateProcessor),
) : PostRenderer by base {
    override fun generateResources(rendered: CharSequence): Set<OutputResource> =
        buildSet {
            // The main HTML resource.
            this.addAll(base.generateResources(rendered))

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
            artifact("plain", condition = context.documentInfo.type == DocumentType.PLAIN)
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
