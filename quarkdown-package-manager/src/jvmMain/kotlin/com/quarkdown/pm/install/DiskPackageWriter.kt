package com.quarkdown.pm.install

import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.filesystem.toFileOrNull
import com.quarkdown.pm.model.RepositoryPackage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * [PackageWriter] onto the physical disk.
 */
class DiskPackageWriter : PackageWriter {
    /**
     * Writes the files into a temporary sibling directory (`.<name>.<random>`), then atomically moves it over [into].
     * Writes are serialized process-wide.
     */
    override fun write(
        pkg: RepositoryPackage,
        into: FsEntry,
    ) {
        val target =
            into.toFileOrNull()
                ?: throw PackageWriteException("Package directory ${into.fullPath} is not on the disk file system.")

        synchronized(LOCK) {
            val staging = createStaging(target)
            try {
                pkg.files.forEach { file ->
                    File(staging, file.path).apply {
                        parentFile.mkdirs()
                        writeBytes(file.content())
                    }
                }
                target.deleteRecursivelyOrThrow()
                Files.move(staging.toPath(), target.toPath(), StandardCopyOption.ATOMIC_MOVE)
            } catch (e: IOException) {
                throw PackageWriteException(e.message ?: e::class.simpleName.orEmpty())
            } finally {
                staging.deleteRecursivelyOrThrow()
            }
        }
    }

    private fun createStaging(target: File): File {
        val parent = target.absoluteFile.parentFile
        try {
            parent.mkdirs()
            return Files.createTempDirectory(parent.toPath(), ".${target.name}.").toFile()
        } catch (e: IOException) {
            throw PackageWriteException(e.message ?: e::class.simpleName.orEmpty())
        }
    }

    private fun File.deleteRecursivelyOrThrow() {
        if (exists() && !deleteRecursively()) {
            throw PackageWriteException("Could not remove $path.")
        }
    }

    private companion object {
        val LOCK = Any()
    }
}
