package com.quarkdown.core.filesystem

import okio.Path.Companion.toPath
import okio.FileSystem as OkioBackend

/**
 * The Okio-backed [FileSystem] engine shared by [DiskFileSystem] and [VirtualFileSystem],
 * which differ only in the backend they delegate I/O to.
 * @param backend the Okio engine performing the actual I/O
 * @param workingDirectory optional working directory to resolve relative paths from
 * @param root the file system this one was branched from, if any
 * @param owner the public file system this engine backs, used as the lineage root of its branches
 */
internal class OkioFileSystem(
    internal val backend: OkioBackend,
    override val workingDirectory: FsEntry?,
    override val root: FileSystem? = null,
    private val owner: FileSystem? = null,
) : FileSystem {
    /**
     * The file system that branches of this one consider their [FileSystem.root]:
     * the original root if already branched, otherwise the public [owner] (or this engine itself).
     */
    private val lineageRoot: FileSystem
        get() = root ?: owner ?: this

    override fun resolve(path: String): FsEntry =
        when {
            workingDirectory != null && !path.toPath().isAbsolute -> workingDirectory.resolve(path)
            else -> FsEntry(path.toPath(), backend)
        }

    override fun branch(workingDirectory: FsEntry?): FileSystem {
        requireSameBackend(workingDirectory)
        return OkioFileSystem(backend, workingDirectory, lineageRoot)
    }

    override fun reroot(workingDirectory: FsEntry?): FileSystem {
        requireSameBackend(workingDirectory)
        return OkioFileSystem(backend, workingDirectory)
    }

    /**
     * Ensures that a working directory candidate belongs to this file system's backend.
     * @throws IllegalArgumentException if [entry] was produced by a different backend
     */
    private fun requireSameBackend(entry: FsEntry?) {
        require(entry == null || entry.backend == backend) {
            "The working directory belongs to a different file system backend."
        }
    }

    override fun relativePathTo(other: FileSystem): FsEntry? {
        val from = this.workingDirectory?.canonical ?: return null
        val to = other.workingDirectory?.canonical ?: return null
        return to.relativeTo(from)
    }

    override fun equals(other: Any?): Boolean {
        val otherEngine =
            when (other) {
                is OkioBackedFileSystem -> other.engine
                is OkioFileSystem -> other
                else -> return false
            }
        return backend == otherEngine.backend &&
            workingDirectory == otherEngine.workingDirectory &&
            root == otherEngine.root
    }

    override fun hashCode(): Int = 31 * backend.hashCode() + workingDirectory.hashCode()
}

/**
 * Base class for the public [FileSystem] entry points ([DiskFileSystem], [VirtualFileSystem]),
 * which are always root file systems and delegate their behavior to the internal [OkioFileSystem] engine,
 * while acting themselves as the lineage root of their [branch]es.
 */
abstract class OkioBackedFileSystem internal constructor(
    backend: OkioBackend,
    workingDirectory: FsEntry?,
) : FileSystem {
    internal val engine = OkioFileSystem(backend, workingDirectory, root = null, owner = this)

    final override val workingDirectory: FsEntry?
        get() = engine.workingDirectory

    final override val root: FileSystem?
        get() = null

    final override fun resolve(path: String): FsEntry = engine.resolve(path)

    final override fun branch(workingDirectory: FsEntry?): FileSystem = engine.branch(workingDirectory)

    final override fun reroot(workingDirectory: FsEntry?): FileSystem = engine.reroot(workingDirectory)

    final override fun relativePathTo(other: FileSystem): FsEntry? = engine.relativePathTo(other)

    /**
     * File systems are compared by value: same backend, working directory, and branch lineage.
     * This mirrors the behavior expected by recursive AST comparisons.
     */
    final override fun equals(other: Any?): Boolean = engine == other

    final override fun hashCode(): Int = engine.hashCode()
}
