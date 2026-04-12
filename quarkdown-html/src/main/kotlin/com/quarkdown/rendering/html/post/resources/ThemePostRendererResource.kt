package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.localization.Locale
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.installlayout.InstallLayout
import com.quarkdown.installlayout.InstallLayoutDirectory

/**
 * A [PostRendererResource] that bundles CSS theme components and their sibling assets
 * (fonts, images) for styling the HTML output.
 *
 * Themes are read from [themeLayout], an [InstallLayout.Html.Themes] node populated by the
 * `assembleThemes` Gradle task. In a Quarkdown distribution it ends up under `lib/html/theme/`.
 *
 * Expected layout:
 * ```
 * <themeDirectory>/
 *   global.css
 *   layout/<name>/<name>.css   (+ exported font/asset subfolders)
 *   color/<name>/<name>.css
 *   locale/<tag>/<tag>.css     (+ optional exported assets, e.g. CJK fonts)
 * ```
 *
 * Active components include global styles (always), the selected layout and color
 * themes, and the locale-specific stylesheet if one exists (e.g. CJK typefaces, #105).
 * Each component may carry sibling assets next to its CSS, and a generated `theme.css`
 * manifest imports them all via nested paths that mirror the output directory layout.
 *
 * If [themeLayout] is `null` or does not exist, no theme resources are emitted; this keeps
 * theme-independent tests easy to construct.
 *
 * @param theme the document theme specifying color and layout preferences
 * @param locale the optional locale for locale-specific styling
 * @param themeLayout the install layout node for the `theme/` directory
 */
class ThemePostRendererResource(
    private val theme: DocumentTheme,
    private val locale: Locale?,
    private val themeLayout: InstallLayout.Html.Themes?,
) : PostRendererResource {
    companion object {
        private const val MANIFEST_NAME = "theme"

        /**
         * Manifest `@import` path for a component with the given artifact [name],
         * relative to the theme directory root. Shared between production and tests
         * to avoid drift.
         *
         * File-backed entries (e.g. `global.css`, `locale/zh.css`) already carry the
         * `.css` extension and map to themselves. Directory-backed entries (e.g.
         * `layout/latex`) expand to `<name>/<last>.css` to reach the CSS file inside
         * the theme folder.
         */
        internal fun importPathFor(name: String): String =
            when {
                name.endsWith(".css") -> name
                else -> "$name/${name.substringAfterLast('/')}.css"
            }
    }

    /**
     * A resolved theme component ready to be emitted as an output artifact.
     *
     * [name] preserves the subdirectory structure (e.g. `layout/latex`) so the whole
     * theme folder is copied into the output. [source] is the file or directory to copy.
     */
    private data class Component(
        val name: String,
        val source: java.io.File,
    ) {
        val importPath: String get() = importPathFor(name)
    }

    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        val components = resolveComponents() ?: return
        resources +=
            OutputResourceGroup(
                name = MANIFEST_NAME,
                resources = buildArtifacts(components),
            )
    }

    /**
     * Builds the artifact set for a theme group: one [FileReferenceOutputArtifact] per
     * [Component] plus a single-entry `theme.css` manifest that `@import`s them all.
     */
    private fun buildArtifacts(components: List<Component>): Set<OutputResource> =
        buildSet {
            components.mapTo(this) { FileReferenceOutputArtifact(name = it.name, file = it.source) }
            add(
                TextOutputArtifact(
                    name = MANIFEST_NAME,
                    content = components.joinToString(separator = "\n") { "@import url('${it.importPath}');" },
                    type = ArtifactType.CSS,
                ),
            )
        }

    /**
     * Resolves the set of active theme [Component]s from [themeLayout].
     * Returns `null` if [themeLayout] is absent or does not exist on disk.
     * Missing layout or color themes are logged and skipped rather than raising,
     * so a broken theme reference degrades gracefully; missing locales are silently
     * skipped, since most locales have no stylesheet.
     */
    private fun resolveComponents(): List<Component>? {
        if (themeLayout?.exists() != true) return null

        return buildList {
            themeLayout.global
                .takeIf { it.exists() }
                ?.let { add(Component(name = it.name, source = it.file)) }

            resolveComponent(themeLayout.layout, theme.layout, warnIfMissing = true)?.let(::add)
            resolveComponent(themeLayout.color, theme.color, warnIfMissing = true)?.let(::add)
            resolveComponent(themeLayout.locale, locale?.shortTag, warnIfMissing = false)?.let(::add)
        }
    }

    /**
     * Resolves a `<kind>/<name>` theme directory under [kindDirectory], or `null` when missing.
     * If [warnIfMissing] is set, a missing directory is logged at error level so a broken
     * theme reference is visible while still producing usable output; otherwise (e.g. for
     * locales, where most tags have no stylesheet) the absence is silently ignored.
     */
    private fun resolveComponent(
        kindDirectory: InstallLayoutDirectory,
        name: String?,
        warnIfMissing: Boolean,
    ): Component? {
        if (name == null) return null

        val directory = kindDirectory.resolveDirectory(name)
        if (!directory.exists()) {
            if (warnIfMissing) {
                Log.error(
                    "'${kindDirectory.name}' theme not found: $name (looked in ${directory.file.absolutePath}).\n" +
                        "For a list of available themes, check https://quarkdown.com/wiki/themes or your local theme directory.",
                )
            }
            return null
        }
        return Component(name = "${kindDirectory.name}/$name", source = directory.file)
    }
}
