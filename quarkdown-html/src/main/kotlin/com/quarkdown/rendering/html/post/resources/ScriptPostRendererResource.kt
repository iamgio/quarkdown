package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.rendering.html.post.resources.ScriptPostRendererResource.Companion.SCRIPT_FILES
import java.io.File

/**
 * Output directory name under which the Quarkdown runtime script is emitted.
 * Shared between this resource and the HTML document builder that loads `<path>/quarkdown.min.js`,
 * so the producer and the consumer cannot drift apart.
 */
const val HTML_SCRIPT_OUTPUT_PATH = "script"

/** Main Quarkdown runtime script file, bundled by the `bundleTypeScript` Gradle task. */
const val HTML_SCRIPT_FILE_NAME = "quarkdown.min.js"

/**
 * A [PostRendererResource] that copies the pre-bundled `quarkdown.min.js` runtime and its source
 * map next to the HTML output, enabling fully offline rendering.
 *
 * The script is built once by the `bundleTypeScript` Gradle task and shipped inside a Quarkdown
 * installation under `lib/html/`, alongside the other third-party assets. At render time, this
 * resource reads that same directory and emits [FileReferenceOutputArtifact]s for each required
 * file.
 *
 * If [libraryDirectory] is `null` or does not exist, no script resources are emitted; this keeps
 * script-independent tests easy to construct. Otherwise, every file listed in [SCRIPT_FILES] must
 * be present — any missing file indicates a broken Quarkdown installation and raises
 * [IllegalStateException].
 *
 * @param libraryDirectory the filesystem directory containing the bundled `quarkdown.min.js`,
 *        typically `lib/html` within the Quarkdown installation
 */
class ScriptPostRendererResource(
    private val libraryDirectory: File?,
) : PostRendererResource {
    companion object {
        private val SCRIPT_FILES = listOf(HTML_SCRIPT_FILE_NAME, "$HTML_SCRIPT_FILE_NAME.map")
    }

    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        val root = libraryDirectory?.takeIf(File::isDirectory) ?: return
        val artifacts =
            SCRIPT_FILES
                .map { name ->
                    val file = root.resolve(name)
                    check(file.isFile) {
                        "Required Quarkdown runtime script file is missing: ${file.absolutePath}. " +
                            "This likely indicates a broken Quarkdown installation."
                    }
                    FileReferenceOutputArtifact(name = file.name, file = file)
                }.toSet()

        resources += OutputResourceGroup(name = HTML_SCRIPT_OUTPUT_PATH, resources = artifacts)
    }
}
