package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.toOutputResource

private const val STATIC_ASSETS_DIRECTORY_NAME = "public"

/**
 * A [PostRendererResource] that copies the contents of a `public/` directory, located at the
 * project's root ([parentDirectory]), directly into the output root.
 *
 * This allows users to ship static files (e.g. `robots.txt`, `CNAME`, `favicon.ico`) alongside
 * the rendered document without any processing. The files land at the top level of the output resources.
 *
 * Disk-backed directories are copied efficiently by reference, while virtual directories
 * are walked and materialized into in-memory artifacts.
 *
 * If the `public/` directory does not exist, no resources are emitted.
 *
 * @param parentDirectory the project's working directory in which to look for a `public/` subdirectory
 */
class StaticAssetsPostRendererResource(
    private val parentDirectory: FsEntry,
) : PostRendererResource {
    override fun includeTo(
        resources: MutableSet<OutputResource>,
        rendered: CharSequence,
    ) {
        val staticAssetsDirectory = parentDirectory.resolve(STATIC_ASSETS_DIRECTORY_NAME)
        if (!staticAssetsDirectory.isDirectory) return

        resources += staticAssetsDirectory.toOutputResource(name = ".")
    }
}
