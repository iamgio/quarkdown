package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.context.Context
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.installlayout.InstallLayoutDirectory
import com.quarkdown.rendering.html.post.thirdparty.ThirdPartyLibrary

const val HTML_LIBRARY_OUTPUT_PATH = "lib"

/**
 * A [PostRendererResource] that bundles required third-party libraries (scripts, styles, fonts) into the output,
 * enabling fully offline HTML rendering.
 *
 * Library inclusion is driven by [ThirdPartyLibrary] for global scripts and styles.
 *
 * Since subdocuments share the root's `lib/` directory (they reference it via a relative path),
 * the set of bundled libraries is the union of those required by the root context and by every
 * subdocument context in the same document complex.
 *
 * @param context the root rendering context, used (together with its subdocument contexts) to evaluate library inclusion conditions
 * @param librariesLayout the install layout node for the `lib/` directory containing third-party
 *        library files, typically [InstallLayout.Html.libraries][com.quarkdown.installlayout.InstallLayout.Html.libraries].
 *        If `null`, no libraries are bundled.
 */
class ThirdPartyPostRendererResource(
    private val context: Context,
    private val librariesLayout: InstallLayoutDirectory?,
) : PostRendererResource {
    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        if (librariesLayout?.exists() != true) return

        // Include a library if it is required by the root context or by any subdocument context,
        // since the same root `lib/` directory is shared.
        val allContexts = context.sharedSubdocumentsData.withContexts.values
        val libraryNames =
            ThirdPartyLibrary
                .all()
                .filter { library -> allContexts.any(library::isRequired) }
                .flatMap { it.names }

        resources +=
            OutputResourceGroup(
                name = HTML_LIBRARY_OUTPUT_PATH,
                resources =
                    libraryNames
                        .map { libraryName ->
                            librariesLayout
                                .resolveDirectory(libraryName)
                                .also { if (!it.exists()) error("HTML library directory not found: ${it.file.path}") }
                                .asOutputResource()
                        }.toSet(),
            )
    }
}
