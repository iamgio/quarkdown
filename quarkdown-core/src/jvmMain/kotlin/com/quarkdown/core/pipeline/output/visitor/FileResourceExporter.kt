package com.quarkdown.core.pipeline.output.visitor

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.filesystem.toFileOrNull
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.FileReferenceOutputArtifact
import com.quarkdown.core.pipeline.output.OutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.OutputResourceGroup
import com.quarkdown.core.pipeline.output.OutputResourceNaming.fileNameWithoutExtension
import com.quarkdown.core.pipeline.output.OutputResourceNaming.fullFileName
import com.quarkdown.core.pipeline.output.OutputResourceVisitor
import com.quarkdown.core.pipeline.output.TextOutputArtifact
import com.quarkdown.core.util.IOUtils
import java.io.File
import java.nio.file.Files

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
     * Exports a [FileReferenceOutputArtifact] to the output location.
     * If the source is a directory, it is copied recursively.
     *
     * Resolution order for disk-backed sources:
     * 1. If [FileReferenceOutputArtifact.symlink] is set and the platform supports symbolic links,
     *    a link is created instead of copying.
     * 2. Otherwise, if [FileReferenceOutputArtifact.useChecksumInvalidation] is enabled, a sibling
     *    `.checksum` file is maintained next to the output; the copy is skipped when the source's
     *    checksum matches the stored value. This avoids redundant I/O for large assets (fonts,
     *    third-party libraries) that rarely change between builds.
     * 3. Otherwise, the source is copied unconditionally.
     *
     * A virtual (non-disk-backed) source is materialized by reading it through its file system,
     * without symlinking or checksum invalidation.
     *
     * @return the copied file or directory
     */
    override fun visit(artifact: FileReferenceOutputArtifact) =
        File(location, artifact.name).also { target ->
            if (!write) return@also

            target.parentFile?.mkdirs()

            val source = artifact.file.toFileOrNull()
            when {
                source == null -> materialize(artifact.file, target)
                artifact.symlink && IOUtils.trySymlink(target.toPath(), source.toPath()) -> {}
                artifact.useChecksumInvalidation -> copyWithChecksumInvalidation(artifact.name, source, target)
                else -> copyFileOrDirectory(source, target)
            }
        }

    private fun copyWithChecksumInvalidation(
        name: String,
        source: File,
        target: File,
    ) {
        val checksumFile = target.resolveSibling("${target.name}.checksum")
        val currentChecksum = IOUtils.computeChecksum(source)
        val storedChecksum = checksumFile.takeIf { it.isFile }?.readText()

        if (currentChecksum == storedChecksum && target.exists() && !Files.isSymbolicLink(target.toPath())) {
            Log.debug { "Skipping '$name': checksum unchanged ($currentChecksum)" }
            return
        }

        Log.debug {
            "Copying '$name': checksum changed " +
                "(stored=${storedChecksum ?: "<none>"}, current=$currentChecksum)"
        }

        copyFileOrDirectory(source, target)
        checksumFile.writeText(currentChecksum)
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
     * Writes a virtual (non-disk-backed) [FsEntry] to [target] by reading it through its file system,
     * recursing into directories.
     */
    private fun materialize(
        entry: FsEntry,
        target: File,
    ) {
        if (entry.isDirectory) {
            target.mkdirs()
            entry.children().forEach { materialize(it, File(target, it.name)) }
        } else {
            target.writeBytes(entry.readBytes())
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
