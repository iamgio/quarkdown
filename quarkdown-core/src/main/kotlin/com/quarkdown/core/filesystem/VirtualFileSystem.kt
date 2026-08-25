package com.quarkdown.core.filesystem

import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem

/**
 * An in-memory [FileSystem], detached from the physical disk.
 * Entries produced by this file system return `null` from [FsEntry.toFileOrNull].
 * @param workingDirectoryPath absolute path of the working directory within the virtual tree
 */
class VirtualFileSystem private constructor(
    private val backend: FakeFileSystem,
    workingDirectoryPath: String?,
) : OkioBackedFileSystem(
        backend,
        workingDirectoryPath?.let { FsEntry(it.toPath(), backend) },
    ) {
    constructor(workingDirectoryPath: String? = "/") : this(FakeFileSystem(), workingDirectoryPath)

    /**
     * Creates a directory at [path] (relative to the working directory, or absolute),
     * including any missing parent directories.
     */
    fun mkdirs(path: String) {
        backend.createDirectories(resolve(path).path)
    }

    /**
     * Writes UTF-8 [content] to the file at [path] (relative to the working directory, or absolute),
     * creating missing parent directories.
     */
    fun write(
        path: String,
        content: String,
    ) {
        val entry = resolve(path)
        entry.parent?.let { backend.createDirectories(it.path, mustCreate = false) }
        backend.write(entry.path) { writeUtf8(content) }
    }

    /**
     * Writes raw [content] bytes to the file at [path] (relative to the working directory, or absolute),
     * creating missing parent directories.
     */
    fun writeBytes(
        path: String,
        content: ByteArray,
    ) {
        val entry = resolve(path)
        entry.parent?.let { backend.createDirectories(it.path, mustCreate = false) }
        backend.write(entry.path) { write(content) }
    }
}
