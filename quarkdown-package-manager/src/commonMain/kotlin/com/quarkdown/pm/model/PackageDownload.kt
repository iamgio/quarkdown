package com.quarkdown.pm.model

import com.quarkdown.core.filesystem.VirtualFileSystem

/**
 * Loads every file of this package into a new in-memory file system, rooted at `/`.
 * The result can be read through the regular file system API, included into a document,
 * or exported to disk via `FsEntry.toOutputResource()`.
 * @return the populated file system
 * @throws com.quarkdown.pm.net.RemoteFetchException if any file cannot be downloaded
 */
fun RepositoryPackage.download(): VirtualFileSystem =
    VirtualFileSystem().also { fs ->
        files.forEach { file -> fs.writeBytes(file.path, file.content()) }
    }
