package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.context.Context
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.rendering.html.post.thirdparty.ThirdPartyLibrary
import java.io.File

const val HTML_LIBRARY_OUTPUT_PATH = "lib"

/**
 * A [PostRendererResource] that bundles required third-party libraries (scripts, styles, fonts) into the output,
 * enabling fully offline HTML rendering.
 *
 * Library inclusion is driven by [ThirdPartyLibrary] for global scripts and styles,
 * and by [LayoutThemeManifest] for styles and fonts declared by the active layout theme.
 *
 * @param context the rendering context, used to evaluate library inclusion conditions
 * @param libraryDirectory the filesystem directory containing third-party library files,
 *        typically `lib/html` within the Quarkdown installation. If `null`, no libraries are bundled.
 */
class ThirdPartyPostRendererResource(
    private val context: Context,
    private val libraryDirectory: File?,
) : PostRendererResource {
    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        if (libraryDirectory == null || !libraryDirectory.isDirectory) return

        val libraryNames =
            ThirdPartyLibrary
                .all()
                .filter { it.isRequired(context) }
                .flatMap { it.names }

        resources +=
            OutputResourceGroup(
                name = HTML_LIBRARY_OUTPUT_PATH,
                resources =
                    libraryNames
                        .map { libraryName ->
                            val file = libraryDirectory.resolve(libraryName)
                            FileReferenceOutputArtifact(
                                name = libraryName,
                                file =
                                    file.takeIf(File::isDirectory)
                                        ?: error("HTML library directory not found: ${file.path}"),
                            )
                        }.toSet(),
            )
    }
}
