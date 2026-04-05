package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.context.Context
import com.quarkdown.core.document.DocumentTheme
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.rendering.html.post.thirdparty.ThirdPartyLibrary

/**
 * A [PostRendererResource] that bundles third-party libraries (scripts, styles, fonts) into the output,
 * enabling fully offline HTML rendering without any CDN dependencies.
 *
 * Library inclusion is driven entirely by [ThirdPartyLibrary], the single source of truth
 * for each library's condition and identity. This class simply filters the required libraries
 * and delegates file loading to [ThirdPartyResourceLoader].
 *
 * @param context the rendering context, used to evaluate library inclusion conditions
 * @param theme the active document theme, used to determine which font libraries to include
 */
class ThirdPartyPostRendererResource(
    private val context: Context,
    private val theme: DocumentTheme,
) : PostRendererResource {
    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        val requiredNames =
            ThirdPartyLibrary
                .all(theme)
                .filter { it.isRequired(context) }
                .map { it.name }

        if (requiredNames.isEmpty()) return

        resources += ThirdPartyResourceLoader.loadAll("lib", requiredNames)
    }
}
