package com.quarkdown.core.pipeline.output.visitor

import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.OutputResourceVisitor
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter.NameProvider.fileNameWithoutExtension
import com.quarkdown.core.pipeline.output.visitor.FileResourceExporter.NameProvider.fullFileName
import com.quarkdown.core.util.IOUtils
import com.quarkdown.core.util.sanitizeFileName
import java.io.File

/**
 * A visitor that saves each type of [OutputResource] to a file and returns it.
 * @param location directory to save the resources to
 * @param write whether to actually write to the file system (if `false`, the visitor only returns the corresponding file paths without creating them)
 */
class FileResourceExporter(
    private val location: File,
    private val write: Boolean = true,
) : OutputResourceVisitor<File> {
    /**
     * Mapping of [OutputResource]s to their file names.
     */
    object NameProvider {
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

    /**
     * Saves an [OutputArtifact] to a file with text content.
     * @return the file itself
     */
    override fun visit(artifact: TextOutputArtifact) =
        File(location, artifact.fullFileName).also {
            if (write) {
                it.parentFile?.mkdirs()
                it.writeText(artifact.content.toString())
            }
        }

    override fun visit(artifact: BinaryOutputArtifact) =
        File(location, artifact.fullFileName).also {
            if (write) {
                it.parentFile?.mkdirs()
                it.writeBytes(artifact.content.toByteArray())
            }
        }

    /**
     * Copies a [FileReferenceOutputArtifact] to the output location.
     * If the source is a directory, it is copied recursively.
     *
     * When [FileReferenceOutputArtifact.useChecksumInvalidation] is enabled, a sibling
     * `.checksum` file is maintained next to the output. If the checksum of the source
     * matches the stored value, the copy is skipped entirely. This avoids redundant I/O
     * for large assets (fonts, third-party libraries) that rarely change between builds.
     *
     * @return the copied file or directory
     */
    override fun visit(artifact: FileReferenceOutputArtifact) =
        File(location, artifact.name).also { target ->
            if (!write) return@also

            target.parentFile?.mkdirs()

            if (artifact.useChecksumInvalidation) {
                val checksumFile = target.resolveSibling("${target.name}.checksum")
                val currentChecksum = IOUtils.computeChecksum(artifact.file)
                val storedChecksum = checksumFile.takeIf { it.isFile }?.readText()

                if (currentChecksum == storedChecksum) {
                    Log.debug { "Skipping '${artifact.name}': checksum unchanged ($currentChecksum)" }
                    return@also
                }

                Log.debug {
                    "Copying '${artifact.name}': checksum changed " +
                        "(stored=${storedChecksum ?: "<none>"}, current=$currentChecksum)"
                }
                copyFileOrDirectory(artifact.file, target)
                checksumFile.writeText(currentChecksum)
            } else {
                copyFileOrDirectory(artifact.file, target)
            }
        }

    private fun copyFileOrDirectory(
        source: File,
        target: File,
    ) {
        if (source.isDirectory) {
            source.copyRecursively(target, overwrite = true)
        } else {
            source.copyTo(target, overwrite = true)
        }
    }

    /**
     * Saves an [OutputResourceGroup] to a directory which contains its nested files.
     * @return the directory file itself
     */
    override fun visit(group: OutputResourceGroup): File {
        val directory = File(location, group.fileNameWithoutExtension)

        // The directory is not created if it has no content.
        if (group.resources.isEmpty()) {
            return directory
        }

        if (write) directory.mkdirs()

        // Saves the subfiles in the new directory.
        group.resources.forEach {
            it.accept(FileResourceExporter(directory, write))
        }

        return directory
    }
}

/**
 * Saves [this] resource to file in a [directory].
 * @see FileResourceExporter
 * @return the saved file
 */
fun OutputResource.saveTo(directory: File): File = accept(FileResourceExporter(location = directory))
