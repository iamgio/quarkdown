package com.quarkdown.rendering.html.post.resources

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Resolves resource requirements declared by a layout theme's JSON manifest.
 *
 * Each layout theme may declare its dependencies in a JSON manifest file
 * at `/render/theme/layout/{layoutName}.json`, for example:
 * ```json
 * {"fonts": ["fonts/lato", "fonts/inter"]}
 * ```
 *
 * Font names correspond to directory paths relative to the third-party library directory
 * (e.g. `"fonts/lato"`, `"fonts/latex"`). SCSS layout themes reference these fonts via `@import url()`,
 * and this class ensures the corresponding files are bundled in the output.
 *
 * @param fonts third-party font library names required by this layout theme
 */
@Serializable
data class LayoutThemeManifest(
    val fonts: List<String> = emptyList(),
) {
    companion object {
        private val cache = mutableMapOf<String, LayoutThemeManifest?>()

        /**
         * Loads the manifest for the given [layoutName],
         * or `null` if the layout has no manifest. Results are cached by name.
         */
        fun load(layoutName: String?): LayoutThemeManifest? {
            if (layoutName == null) return null
            return cache.getOrPut(layoutName) { parse(layoutName) }
        }

        private fun parse(layoutName: String): LayoutThemeManifest? {
            val json =
                LayoutThemeManifest::class.java
                    .getResource("/render/theme/layout/$layoutName.json")
                    ?.readText()
                    ?: return null

            return Json.decodeFromString<LayoutThemeManifest>(json)
        }
    }
}
