package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.localization.Locale
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact

/**
 * A [PostRendererResource] that includes CSS theme components for styling the HTML output.
 *
 * Theme components include:
 * - Global styles (always included)
 * - Layout-specific styles (e.g., `latex`)
 * - Color scheme styles (e.g., `paperwhite`)
 * - Locale-specific styles (e.g., for CJK typefaces)
 *
 * A `theme.css` manifest file is generated that imports all active theme components.
 *
 * @param theme the document theme specifying color and layout preferences
 * @param locale the optional locale for locale-specific styling (e.g., Chinese typefaces)
 */
class ThemePostRendererResource(
    private val theme: DocumentTheme,
    private val locale: Locale?,
) : PostRendererResource {
    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        resources +=
            OutputResourceGroup(
                name = "theme",
                resources = retrieveThemeComponentsArtifacts(theme, locale),
            )
    }

    private fun getFullResourcePath(resourceSubPath: String): String = "/render/theme/$resourceSubPath.css"

    /**
     * Pushes a new output artifact to the set if it exists.
     * @param resourceName name of the resource
     * @param resourcePath path of the resource starting from the theme folder, without extension
     */
    private fun MutableSet<OutputResource>.artifact(
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

    /**
     * @param theme theme to get the artifacts for
     * @return a set that contains an output artifact for each non-null theme component of [theme]
     *         (e.g. color scheme, layout format, ...)
     */
    private fun retrieveThemeComponentsArtifacts(
        theme: DocumentTheme?,
        locale: Locale?,
    ): Set<OutputResource> =
        buildSet {
            // Pushing theme components.
            artifact("global")
            theme?.layout?.let { artifact(it, "layout/$it") }
            theme?.color?.let { artifact(it, "color/$it") }

            // In case the active locale features its own theme components, add them as well.
            // For example, Chinese typefaces (#105).
            locale?.shortTag?.let {
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
}
