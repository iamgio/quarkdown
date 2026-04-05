package com.quarkdown.rendering.html.post.resources

import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.LazyOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.rendering.html.post.resources.ThirdPartyResourceLoader.FILES_KEY
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Loads third-party library files from the JAR classpath, using a manifest file
 * to discover available files per library.
 *
 * The manifest (`third-party-manifest.json`) is generated at Gradle build time by the `bundleThirdParty` task.
 * Each top-level key is a library name (e.g. `"katex"`, `"fonts/latex"`), and its value is a nested
 * JSON tree of that library's internal directory structure, with files listed under `_files`:
 *
 * ```json
 * {
 *   "katex": {
 *     "_files": ["katex.min.css", "katex.min.js"],
 *     "fonts": { "_files": ["KaTeX_Main-Regular.woff2"] }
 *   },
 *   "fonts/latex": {
 *     "_files": ["fonts.css", "ComputerModern-Serif-Regular.woff"]
 *   }
 * }
 * ```
 *
 * Since JAR directories cannot be listed at runtime, this manifest-driven approach
 * is the only reliable way to enumerate bundled library files.
 */
object ThirdPartyResourceLoader {
    private const val MANIFEST_PATH = "/render/lib/third-party-manifest.json"
    private const val FILES_KEY = "_files"

    private val manifest: JsonObject by lazy {
        val content =
            ThirdPartyResourceLoader::class.java
                .getResource(MANIFEST_PATH)
                ?.readText()
                ?: error("Third-party manifest not found at $MANIFEST_PATH")
        Json.parseToJsonElement(content) as JsonObject
    }

    /**
     * Checks whether the manifest contains an entry for the given [libraryName].
     */
    fun contains(libraryName: String): Boolean = libraryName in manifest

    /**
     * Loads all files belonging to the given [libraryName] from the JAR classpath
     * and wraps them in a nested [OutputResourceGroup] tree that mirrors the directory structure.
     *
     * The [libraryName] may contain path separators (e.g. `"fonts/latex"`), in which case
     * nested [OutputResourceGroup]s are created to match the on-disk structure, since
     * [OutputResourceGroup] names cannot contain slashes.
     *
     * @param libraryName the library key as listed in the manifest (e.g. `"katex"`, `"fonts/latex"`)
     * @return an [OutputResourceGroup] tree containing all the library's files,
     *         or `null` if the library is not in the manifest
     */
    fun load(libraryName: String): OutputResourceGroup? {
        val libraryObject = manifest[libraryName]?.jsonObject ?: return null
        val basePath = "/render/lib/$libraryName"

        val innerGroup = buildGroupFromJson(libraryName.substringAfterLast('/'), libraryObject, basePath)

        // Wrap in nested groups for each parent segment of the library name.
        // e.g. "fonts/latex" -> OutputResourceGroup("fonts", { OutputResourceGroup("latex", ...) })
        val parentSegments = libraryName.split("/").dropLast(1)
        return parentSegments.foldRight(innerGroup) { segment, inner ->
            OutputResourceGroup(segment, setOf(inner))
        }
    }

    /**
     * Loads multiple libraries and merges them into a single [OutputResourceGroup] under the given [groupName].
     * Groups that share a common top-level directory name are merged together.
     *
     * @param groupName name of the resulting resource group (used as the output directory name)
     * @param libraryNames the library names to load
     * @return an [OutputResourceGroup] containing all loaded libraries as nested groups
     */
    fun loadAll(
        groupName: String,
        libraryNames: Iterable<String>,
    ): OutputResourceGroup {
        val loadedGroups = libraryNames.mapNotNull { load(it) }

        // Merge groups that share the same top-level name (e.g. multiple font libraries under "fonts").
        val merged = mergeGroupsByName(loadedGroups)

        return OutputResourceGroup(groupName, merged)
    }

    /**
     * Recursively builds an [OutputResourceGroup] from a manifest JSON object.
     * Files listed under [FILES_KEY] become [LazyOutputArtifact]s;
     * other keys become child [OutputResourceGroup]s.
     */
    private fun buildGroupFromJson(
        name: String,
        json: JsonObject,
        resourceBasePath: String,
    ): OutputResourceGroup {
        val resources = mutableSetOf<OutputResource>()

        // Load files at this level.
        json[FILES_KEY]?.jsonArray?.forEach { fileElement ->
            val fileName = fileElement.jsonPrimitive.content
            LazyOutputArtifact
                .internalOrNull(
                    resource = "$resourceBasePath/$fileName",
                    name = fileName,
                    type = ArtifactType.AUTO,
                )?.let { resources += it }
        }

        // Recurse into subdirectories.
        for ((key, value) in json) {
            if (key == FILES_KEY) continue
            resources += buildGroupFromJson(key, value.jsonObject, "$resourceBasePath/$key")
        }

        return OutputResourceGroup(name, resources)
    }

    /**
     * Merges [OutputResourceGroup]s that share the same [OutputResource.name].
     * When two groups have the same name, their child resources are combined.
     */
    private fun mergeGroupsByName(groups: List<OutputResourceGroup>): Set<OutputResource> {
        val byName = linkedMapOf<String, MutableSet<OutputResource>>()
        for (group in groups) {
            byName.getOrPut(group.name) { mutableSetOf() } += group.resources
        }
        return byName
            .map { (name, resources) ->
                val (nestedGroups, otherResources) = resources.partition { it is OutputResourceGroup }
                val mergedNested = mergeGroupsByName(nestedGroups.filterIsInstance<OutputResourceGroup>())
                OutputResourceGroup(name, otherResources.toSet() + mergedNested)
            }.toSet()
    }
}
