package com.quarkdown.core.filesystem

import okio.Path.Companion.toOkioPath
import java.io.File
import okio.FileSystem as OkioBackend

/**
 * A [FileSystem] backed by the physical disk file system.
 * This is the default file system used by the pipeline and the CLI.
 * @param workingDirectory optional working directory to resolve relative paths from;
 * if relative, it is made absolute against the process working directory
 */
class DiskFileSystem(
    workingDirectory: File? = null,
) : OkioBackedFileSystem(
        OkioBackend.SYSTEM,
        workingDirectory?.let { FsEntry(it.absoluteFile.toOkioPath(), OkioBackend.SYSTEM) },
    )
