package com.quarkdown.pm.model

import com.quarkdown.core.filesystem.VirtualFileSystem

/**
 * Loads these files into a new in-memory file system, rooted at `/`, each at its package-relative path.
 * The result can be read through the regular file system API, included into a document,
 * or exported to disk via `FsEntry.toOutputResources()`.
 * @return the populated file system
 * @throws com.quarkdown.pm.net.RemoteFetchException if any file cannot be downloaded
 */
fun Iterable<PackageFile>.download(): VirtualFileSystem =
    VirtualFileSystem().also { fs ->
        forEach { file -> fs.writeBytes(file.path, file.content()) }
    }

/**
 * Loads every file of this package into a new in-memory file system, rooted at `/`.
 * @return the populated file system
 * @throws com.quarkdown.pm.net.RemoteFetchException if any file cannot be downloaded
 * @see Iterable.download
 */
fun RepositoryPackage.download(): VirtualFileSystem = files.download()
