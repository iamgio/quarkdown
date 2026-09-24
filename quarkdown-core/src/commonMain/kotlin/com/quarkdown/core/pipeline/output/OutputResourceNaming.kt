package com.quarkdown.core.pipeline.output

import com.quarkdown.core.util.sanitizeFileName

/**
 * Mapping of [OutputResource]s to their file names, shared by every exporter.
 */
object OutputResourceNaming {
    /**
     * Given a string, returns a sanitized version of it to be used as a valid file name.
     * @see sanitizeFileName
     */
    internal fun stringToFileName(string: String): String = string.sanitizeFileName(replacement = "-")

    /**
     * Name of the corresponding file of this resource, without the extension,
     * with symbols removed and spaces replaced with dashes.
     */
    val OutputResource.fileNameWithoutExtension: String
        get() = stringToFileName(name)

    /**
     * File extension relative to the [ArtifactType] of this resource.
     */
    val OutputArtifact<*>.fileExtension: String
        get() =
            when (type) {
                ArtifactType.HTML -> ".html"
                ArtifactType.CSS -> ".css"
                ArtifactType.JAVASCRIPT -> ".js"
                ArtifactType.JSON -> ".json"
                ArtifactType.MARKDOWN -> ".md"
                ArtifactType.PLAIN_TEXT -> ".txt"
                ArtifactType.QUARKDOWN -> ".qd"
                ArtifactType.AUTO -> "" // Assumes the file name already contains an extension.
            }

    /**
     * Full name of the file, including the extension relative to the [ArtifactType] of this resource.
     */
    val OutputArtifact<*>.fullFileName: String
        get() = fileNameWithoutExtension + fileExtension
}
