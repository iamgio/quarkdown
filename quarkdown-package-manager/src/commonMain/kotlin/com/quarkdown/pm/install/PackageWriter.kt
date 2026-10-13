package com.quarkdown.pm.install

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.pm.model.RepositoryPackage

/**
 * Strategy that materializes a package's files into a directory.
 * Implementations own the only write access to the file system in the package manager.
 */
fun interface PackageWriter {
    /**
     * Writes every file of [pkg] under [into], replacing whatever that directory held before.
     * @param pkg the package to write
     * @param into target directory, created if missing
     * @throws PackageWriteException if the files cannot be written
     * @throws com.quarkdown.pm.net.RemoteFetchException if a file's content cannot be downloaded
     */
    fun write(
        pkg: RepositoryPackage,
        into: FsEntry,
    )
}

/**
 * Raised when a [PackageWriter] cannot write a package.
 * @param message description of the failure
 */
class PackageWriteException(
    message: String,
) : RuntimeException(message)
