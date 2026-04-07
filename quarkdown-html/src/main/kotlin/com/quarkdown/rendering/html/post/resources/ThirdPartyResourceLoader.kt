package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.pipeline.output.FileReferenceOutputResource
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.rendering.html.post.resources.ThirdPartyResourceLoader.buildGroup
import java.io.File

/**
 * Loads third-party library files from a filesystem directory into [OutputResource] trees
 * that mirror the on-disk directory structure.
 *
 * Unlike JAR-based resource loading, this operates directly on the filesystem,
 * eliminating the need for a manifest file since directories can be listed natively.
 *
 * Files are represented as [FileReferenceOutputResource]s, which are copied
 * (not loaded into memory) when saved to the output directory.
 */
object ThirdPartyResourceLoader {
    /**
     * Loads all requested libraries and returns them as a single [OutputResourceGroup].
     *
     * Each path in [libraryPaths] identifies a directory under [baseDirectory]
     * (e.g. `"katex"`, `"fonts/lato"`). Paths that share a common ancestor
     * are placed under a single parent group in the output:
     *
     * ```
     * loadAll(baseDirectory, "lib", ["katex", "fonts/lato", "fonts/latex"])
     * |  OutputResourceGroup("lib")
     *    | OutputResourceGroup("katex", ...)
     *    |  OutputResourceGroup("fonts")
     *       | OutputResourceGroup("lato", ...)
     *       | OutputResourceGroup("latex", ...)
     * ```
     *
     * @param baseDirectory the filesystem directory containing the library files
     * @param groupName the name of the root [OutputResourceGroup]
     * @param libraryPaths paths relative to [baseDirectory], one per library to load
     */
    fun loadAll(
        baseDirectory: File,
        groupName: String,
        libraryPaths: Iterable<String>,
    ): OutputResourceGroup =
        OutputResourceGroup(
            groupName,
            resolve(baseDirectory, libraryPaths.toList()),
        )

    /**
     * Resolves a list of library [paths] against a [parentDirectory].
     *
     * Paths are grouped by their first segment. For each group:
     * - If a path has no remaining segments (it is a leaf), the corresponding directory
     *   is fully materialized into an [OutputResourceGroup] via [buildGroup].
     * - Otherwise, an intermediate [OutputResourceGroup] is created for the shared segment,
     *   and the remaining sub-paths are resolved recursively against the child directory.
     *
     * @param parentDirectory the current filesystem directory to resolve paths against
     * @param paths library paths relative to [parentDirectory] (e.g. `["katex", "fonts/lato"]`)
     */
    private fun resolve(
        parentDirectory: File,
        paths: List<String>,
    ): Set<OutputResource> =
        buildSet {
            paths
                .groupBy(
                    keySelector = { it.substringBefore("/") },
                    valueTransform = { it.substringAfter("/", "") },
                ).forEach { (segment, remainders) ->
                    val childDir = parentDirectory.resolve(segment)
                    if (!childDir.isDirectory) return@forEach

                    if (remainders.any { it.isEmpty() }) {
                        this += buildGroup(childDir)
                    } else {
                        this += OutputResourceGroup(segment, resolve(childDir, remainders))
                    }
                }
        }

    /**
     * Materializes a filesystem directory into an [OutputResourceGroup],
     * including all files as [FileReferenceOutputResource]s and recursing into subdirectories.
     *
     * @param directory the directory to materialize
     */
    private fun buildGroup(directory: File): OutputResourceGroup =
        OutputResourceGroup(
            directory.name,
            buildSet {
                directory.listFiles()?.sorted()?.forEach { child ->
                    when {
                        child.isFile -> this += FileReferenceOutputResource(child.name, child)
                        child.isDirectory -> this += buildGroup(child)
                    }
                }
            },
        )
}
