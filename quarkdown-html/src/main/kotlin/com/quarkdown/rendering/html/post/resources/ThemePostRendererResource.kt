package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.localization.Locale
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import java.io.File

/**
 * A [PostRendererResource] that bundles CSS theme components and their sibling assets
 * (fonts, images) for styling the HTML output.
 *
 * Themes are read from [themeDirectory], a directory populated by the `assembleThemes`
 * Gradle task. In a Quarkdown distribution it ends up under `lib/html/theme/`.
 *
 * Expected layout:
 * ```
 * <themeDirectory>/
 *   global.css
 *   locale/<tag>.css
 *   layout/<name>/<name>.css  (+ exported font/asset subfolders)
 *   color/<name>/<name>.css
 * ```
 *
 * Active components include global styles (always), the selected layout and color
 * themes (with their exported assets), and the locale-specific stylesheet if one
 * exists (e.g. CJK typefaces, #105). A generated `theme.css` manifest imports all
 * of them via nested paths that mirror the output directory layout.
 *
 * If [themeDirectory] is `null` or does not exist, no theme resources are emitted
 * (matching [ThirdPartyPostRendererResource]); this keeps theme-independent tests
 * easy to construct.
 *
 * @param theme the document theme specifying color and layout preferences
 * @param locale the optional locale for locale-specific styling
 * @param themeDirectory the filesystem directory containing bundled themes
 *        (typically `lib/html/theme` within a Quarkdown installation)
 */
class ThemePostRendererResource(
    private val theme: DocumentTheme,
    private val locale: Locale?,
    private val themeDirectory: File?,
) : PostRendererResource {
    companion object {
        private const val MANIFEST_NAME = "theme"
        private const val GLOBAL_STYLESHEET = "global.css"

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

    /** Layout and color themes are the two per-theme subdirectories under the theme root. */
    private enum class Kind(
        val directoryName: String,
    ) {
        LAYOUT("layout"),
        COLOR("color"),
    }

    /**
     * A resolved theme component ready to be emitted as an output artifact.
     *
     * [name] preserves the subdirectory structure (e.g. `layout/latex`) so the whole
     * theme folder is copied into the output. [source] is the file or directory to copy.
     */
    private data class Component(
        val name: String,
        val source: File,
    ) {
        val importPath: String get() = importPathFor(name)
    }

    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        val themeRoot = themeDirectory?.takeIf(File::isDirectory) ?: return
        val components = resolveComponents(themeRoot)
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
     * Resolves the set of active theme [Component]s against [themeRoot].
     * Missing layout or color themes are logged and skipped rather than raising,
     * so a broken theme reference degrades gracefully.
     */
    private fun resolveComponents(themeRoot: File): List<Component> =
        buildList {
            addIfFile(themeRoot.resolve(GLOBAL_STYLESHEET), name = GLOBAL_STYLESHEET)

            theme.layout?.let { resolveThemeDirectory(themeRoot, Kind.LAYOUT, it)?.let(::add) }
            theme.color?.let { resolveThemeDirectory(themeRoot, Kind.COLOR, it)?.let(::add) }

            // Optional locale-specific stylesheet.
            locale?.shortTag?.let { tag ->
                val path = "locale/$tag.css"
                addIfFile(themeRoot.resolve(path), name = path)
            }
        }

    /** Adds a file-backed [Component] to the list if [file] exists. */
    private fun MutableList<Component>.addIfFile(
        file: File,
        name: String,
    ) {
        if (file.isFile) add(Component(name = name, source = file))
    }

    /**
     * Resolves the directory for a `layout` or `color` theme, or `null` when missing.
     * A missing directory is logged at error level and skipped, so a broken theme
     * reference still produces usable output (just without that component).
     *
     * The returned [Component] references the whole theme directory so its CSS plus
     * any exported sibling assets are copied to the output together.
     */
    private fun resolveThemeDirectory(
        themeRoot: File,
        kind: Kind,
        name: String,
    ): Component? {
        val relativePath = "${kind.directoryName}/$name"
        val directory = themeRoot.resolve(relativePath)
        if (!directory.isDirectory) {
            Log.error(
                "'${kind.directoryName}' theme not found: $name (looked in ${directory.absolutePath}).\n" +
                    "For a list of available themes, check https://quarkdown.com/wiki/themes or your local theme directory.",
            )
            return null
        }
        return Component(name = relativePath, source = directory)
    }
}
