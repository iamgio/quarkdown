package com.quarkdown.pm.install

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.pm.model.RepositoryPackage

/**
 * [PackageWriter] into an in-memory [VirtualFileSystem], for tests and for hosts that keep the project in memory.
 * @param fs the file system that owns the target directory
 */
class VirtualFileSystemPackageWriter(
    private val fs: VirtualFileSystem,
) : PackageWriter {
    override fun write(
        pkg: RepositoryPackage,
        into: FsEntry,
    ) {
        fs.delete(into.fullPath)
        fs.mkdirs(into.fullPath)
        pkg.files.forEach { file -> fs.writeBytes(into.resolve(file.path).fullPath, file.content()) }
    }
}
