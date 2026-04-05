package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.context.Context
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.rendering.html.post.thirdparty.ThirdPartyLibrary

/**
 * A [PostRendererResource] that bundles third-party libraries (scripts, styles, fonts) into the output,
 * enabling fully offline HTML rendering without any CDN dependencies.
 *
 * Library inclusion is driven by [ThirdPartyLibrary] for scripts and styles,
 * and by [LayoutThemeManifest] for font libraries declared by the active layout theme.
 * File loading is delegated to [ThirdPartyResourceLoader].
 *
 * @param context the rendering context, used to evaluate library inclusion conditions
 * @param layoutTheme the active layout theme name, used to resolve font dependencies
 */
class ThirdPartyPostRendererResource(
    private val context: Context,
    private val layoutTheme: String?,
) : PostRendererResource {
    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        val libraryNames =
            ThirdPartyLibrary
                .all()
                .filter { it.isRequired(context) }
                .map { it.name }

        val fontNames = LayoutThemeManifest.load(layoutTheme)?.fonts.orEmpty()

        val allNames = libraryNames + fontNames
        if (allNames.isEmpty()) return

        resources += ThirdPartyResourceLoader.loadAll("lib", allNames)
    }
}
