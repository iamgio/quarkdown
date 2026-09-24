package com.quarkdown.core.util

import com.quarkdown.core.log.Log
import okio.HashingSink
import okio.blackholeSink
import okio.buffer
import java.io.File
import java.io.IOException
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createSymbolicLinkPointingTo
import kotlin.io.path.deleteRecursively

/**
 * Utility methods for file-based operations.
 */
object IOUtils {
    /**
     * Computes an SHA-256 digest that represents the current state of [file].
     * - For a regular file, the digest covers its content.
     * - For a directory, the digest covers the sorted list of relative paths and file sizes,
     *   which is fast (metadata only) and catches additions, deletions, and size changes.
     */
    fun computeChecksum(file: File): String {
        val hashingSink = HashingSink.sha256(blackholeSink())
        hashingSink.buffer().use { sink ->
            if (file.isFile) {
                sink.write(file.readBytes())
            } else {
                file
                    .walkTopDown()
                    .filter { it.isFile }
                    .sortedBy { it.relativeTo(file).path }
                    .forEach {
                        sink.writeUtf8(it.relativeTo(file).path)
                        sink.writeByte(0)
                        sink.writeUtf8(it.length().toString())
                        sink.writeByte(0)
                    }
            }
        }
        return hashingSink.hash.hex()
    }

    /**
     * Recursively deletes the file or directory at [path] without following symbolic links:
     * each link entry is removed, but the file or directory the link points to is left untouched.
     * No-op if [path] does not exist.
     */
    @OptIn(ExperimentalPathApi::class)
    fun deleteWithoutFollowingLinks(path: Path) = path.deleteRecursively()

    /**
     * Attempts to (re)create a symbolic link at [target] pointing to [source]. Returns `true` on
     * success, `false` if symbolic links are unavailable on this platform (caller should fall
     * back to a copy-based path).
     */
    fun trySymlink(
        target: Path,
        source: Path,
    ): Boolean =
        try {
            if (isAlreadySymlinkTo(target, source)) {
                Log.debug { "Symlink '${target.fileName}' already points to '$source'; reusing" }
            } else {
                createOrReplaceSymlinkAt(target, source)
                Log.debug { "Symlinked '${target.fileName}' to '$source'" }
            }
            true
        } catch (e: IOException) {
            Log.debug { "Symlink unavailable for '${target.fileName}' (${e.message}); falling back" }
            false
        }

    /** Whether [target] is already a symbolic link whose stored target is exactly [source]. */
    private fun isAlreadySymlinkTo(
        target: Path,
        source: Path,
    ): Boolean = Files.isSymbolicLink(target) && Files.readSymbolicLink(target) == source

    /**
     * Creates a symbolic link at [target] pointing to [source]. If a file, directory, or stale
     * symlink already occupies the target, it is cleared and the creation is retried.
     */
    private fun createOrReplaceSymlinkAt(
        target: Path,
        source: Path,
    ) {
        try {
            target.createSymbolicLinkPointingTo(source)
        } catch (_: FileAlreadyExistsException) {
            deleteWithoutFollowingLinks(target)
            target.createSymbolicLinkPointingTo(source)
        }
    }
}
