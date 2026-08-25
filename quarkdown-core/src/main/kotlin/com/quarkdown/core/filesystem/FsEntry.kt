package com.quarkdown.core.filesystem

import okio.IOException
import okio.Path
import okio.Path.Companion.toOkioPath
import java.io.File
import okio.FileSystem as OkioBackend

/**
 * A platform-neutral handle to a file or directory, bound to the [FileSystem] backend that created it.
 * An entry provides path math, existence and metadata queries, content reading, and directory listing,
 * without exposing the underlying I/O engine.
 *
 * Entries are created via [FileSystem.resolve] or derived from other entries ([resolve], [parent], [children]).
 */
class FsEntry internal constructor(
    internal val path: Path,
    internal val backend: OkioBackend,
) {
    /** The file or directory name, including any extension. */
    val name: String
        get() = path.name

    /** The file name without its last extension. */
    val nameWithoutExtension: String
        get() = name.substringBeforeLast('.')

    /** The file extension, without the leading dot, or an empty string if there is none. */
    val extension: String
        get() = name.substringAfterLast('.', "")

    /** The parent entry, or `null` if this entry has no parent. */
    val parent: FsEntry?
        get() = path.parent?.let { FsEntry(it, backend) }

    /** Whether this entry's path is absolute. */
    val isAbsolute: Boolean
        get() = path.isAbsolute

    /** The full path string of this entry, suitable for display and error messages. */
    val fullPath: String
        get() = path.toString()

    /** [fullPath] with `/` as the separator on every platform, suitable for URLs. */
    val invariantSeparatorsPath: String
        get() = fullPath.replace('\\', '/')

    /** This entry with redundant path segments (`.`, `..`) resolved lexically. */
    val normalized: FsEntry
        get() = FsEntry(path.normalized(), backend)

    /**
     * This entry with symbolic links resolved, if it exists;
     * falls back to its absolute, [normalized] form otherwise.
     *
     * Disk-backed entries resolve through the platform's real-path lookup so to resolve symbolic links on Windows.
     */
    val canonical: FsEntry
        get() {
            toFileOrNull()?.let { file ->
                val real =
                    try {
                        file.toPath().toRealPath()
                    } catch (_: IOException) {
                        file.toPath().toAbsolutePath().normalize()
                    }
                return FsEntry(real.toFile().toOkioPath(), backend)
            }
            return try {
                FsEntry(backend.canonicalize(path), backend)
            } catch (_: IOException) {
                normalized
            }
        }

    /** Whether this entry exists. */
    val exists: Boolean
        get() = backend.exists(path)

    /** Whether this entry exists and is a regular file. */
    val isFile: Boolean
        get() = backend.metadataOrNull(path)?.isRegularFile == true

    /** Whether this entry exists and is a directory. */
    val isDirectory: Boolean
        get() = backend.metadataOrNull(path)?.isDirectory == true

    /** The last modification timestamp in epoch milliseconds, if available. */
    val lastModifiedAtMillis: Long?
        get() = backend.metadataOrNull(path)?.lastModifiedAtMillis

    /**
     * Resolves a [child] path against this entry.
     * @param child relative path to resolve
     * @return the resolved entry, on the same backend
     */
    fun resolve(child: String): FsEntry = FsEntry(path / child, backend)

    /**
     * Computes this entry's path relative to [other].
     * @param other the base entry
     * @return the relative entry, or `null` if a relative path cannot be computed
     *         or the entries belong to different backends
     */
    fun relativeTo(other: FsEntry): FsEntry? {
        if (backend != other.backend) return null
        return try {
            FsEntry(path.relativeTo(other.path), backend)
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /**
     * Whether this entry is located in [ancestor] or any of its subdirectories.
     * Both paths are canonicalized when possible, so a symbolic link inside [ancestor]
     * pointing outside it is correctly detected as not being a sub-path.
     */
    fun isSubPathOf(ancestor: FsEntry): Boolean {
        if (backend != ancestor.backend) return false
        val ancestorPath = ancestor.canonical.path
        return generateSequence(canonical.path) { it.parent }.any { it == ancestorPath }
    }

    /**
     * Lists the direct children of this directory entry.
     * @return the children, or an empty list if this entry is not a listable directory
     */
    fun children(): List<FsEntry> = backend.listOrNull(path).orEmpty().map { FsEntry(it, backend) }

    /**
     * Walks this directory entry recursively.
     * @return all descendant entries, excluding this entry itself
     */
    fun descendants(): Sequence<FsEntry> = children().asSequence().flatMap { sequenceOf(it) + it.descendants() }

    /** @return the full text content of this file, decoded as UTF-8 */
    fun readText(): String = backend.read(path) { readUtf8() }

    /** @return the lines of this file, decoded as UTF-8, without line terminators */
    fun readLines(): List<String> = backend.read(path) { generateSequence(::readUtf8Line).toList() }

    /** @return the raw byte content of this file */
    fun readBytes(): ByteArray = backend.read(path) { readByteArray() }

    /**
     * Converts this entry to a [File], if it is backed by the physical disk file system.
     * This is the boundary between the platform-neutral input abstraction and JVM-only
     * output or process-level code; input-side code must not use it.
     * @return the corresponding [File], or `null` if this entry is virtual
     */
    fun toFileOrNull(): File? = if (backend === OkioBackend.SYSTEM) path.toFile() else null

    override fun equals(other: Any?): Boolean = other is FsEntry && path == other.path && backend == other.backend

    override fun hashCode(): Int = 31 * path.hashCode() + backend.hashCode()

    override fun toString(): String = fullPath
}
