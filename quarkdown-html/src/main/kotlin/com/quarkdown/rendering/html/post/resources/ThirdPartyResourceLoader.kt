package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.rendering.html.post.resources.ThirdPartyResourceLoader.buildGroup
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val FILES_KEY = "_files"

/**
 * Loads third-party library files from the JAR classpath into [OutputResource] trees
 * that mirror the on-disk directory structure.
 *
 * File discovery relies on `third-party-manifest.json`, a nested JSON tree generated
 * at Gradle build time by the `bundleThirdParty` task. The tree mirrors the `lib/` directory,
 * with files at each level listed under `_files` keys:
 * ```json
 * {
 *   "katex": { "_files": ["katex.min.js"], "fonts": { "_files": ["KaTeX_Main.woff2"] } },
 *   "fonts": { "lato": { "_files": ["fonts.css", "lato-latin-400-normal.woff2"] } }
 * }
 * ```
 */
object ThirdPartyResourceLoader {
    private val manifest: JsonObject by lazy {
        val content =
            ThirdPartyResourceLoader::class.java
                .getResource("/render/lib/third-party-manifest.json")
                ?.readText()
                ?: error("Third-party manifest not found")
        Json.parseToJsonElement(content) as JsonObject
    }

    /**
     * Loads all requested libraries and returns them as a single [OutputResourceGroup].
     *
     * Each path in [libraryPaths] identifies a node in the manifest tree
     * (e.g. `"katex"`, `"fonts/lato"`). Paths that share a common ancestor
     * are placed under a single parent group in the output:
     *
     * ```
     * loadAll("lib", ["katex", "fonts/lato", "fonts/latex"])
     * |  OutputResourceGroup("lib")
     *    | OutputResourceGroup("katex", ...)
     *    |  OutputResourceGroup("fonts")
     *       | OutputResourceGroup("lato", ...)
     *       | OutputResourceGroup("latex", ...)
     * ```
     *
     * @param groupName the name of the root [OutputResourceGroup]
     * @param libraryPaths paths into the manifest tree, one per library to load
     */
    fun loadAll(
        groupName: String,
        libraryPaths: Iterable<String>,
    ): OutputResourceGroup =
        OutputResourceGroup(
            groupName,
            resolve(manifest, "/render/lib", libraryPaths.toList()),
        )

    /**
     * Resolves a list of library [paths] against a [parent] manifest node.
     *
     * Paths are grouped by their first segment. For each group:
     * - If a path has no remaining segments (it is a leaf), the corresponding manifest subtree
     *   is fully materialized into an [OutputResourceGroup] via [buildGroup].
     * - Otherwise, an intermediate [OutputResourceGroup] is created for the shared segment,
     *   and the remaining sub-paths are resolved recursively against the child node.
     *
     * @param parent the current manifest JSON node to resolve paths against
     * @param basePath the JAR classpath prefix corresponding to [parent]
     * @param paths library paths relative to [parent] (e.g. `["katex", "fonts/lato"]`)
     */
    private fun resolve(
        parent: JsonObject,
        basePath: String,
        paths: List<String>,
    ): Set<OutputResource> =
        buildSet {
            paths
                .groupBy(
                    keySelector = { it.substringBefore("/") },
                    valueTransform = { it.substringAfter("/", "") },
                ).forEach { (segment, remainders) ->
                    val node = parent[segment]?.jsonObject ?: return@forEach
                    val childPath = "$basePath/$segment"

                    if (remainders.any { it.isEmpty() }) {
                        this += buildGroup(segment, node, childPath)
                    } else {
                        this += OutputResourceGroup(segment, resolve(node, childPath, remainders))
                    }
                }
        }

    /**
     * Materializes a manifest subtree into an [OutputResourceGroup],
     * loading all files and recursing into all subdirectories.
     *
     * @param name the directory name for this group
     * @param json the manifest JSON node whose `_files` and child objects to load
     * @param basePath the JAR classpath prefix for resolving file resources
     */
    private fun buildGroup(
        name: String,
        json: JsonObject,
        basePath: String,
    ): OutputResourceGroup =
        OutputResourceGroup(
            name,
            buildSet {
                json[FILES_KEY]?.jsonArray?.forEach {
                    val fileName = it.jsonPrimitive.content
                    LazyOutputArtifact
                        .internalOrNull("$basePath/$fileName", fileName, ArtifactType.AUTO)
                        ?.let { artifact -> this += artifact }
                }

                for ((key, value) in json) {
                    if (key != FILES_KEY) {
                        this += buildGroup(key, value.jsonObject, "$basePath/$key")
                    }
                }
            },
        )
}
