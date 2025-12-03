package com.quarkdown.core.context.file

import com.quarkdown.core.util.IOUtils
import java.io.File

/**
 * A file system abstraction which can retrieve files,
 * either absolutely or relative to a working directory.
 */
interface FileSystem {
    /**
     * The working directory of this file system.
     * If not `null`, [resolve] will be able to resolve relative paths
     * from this directory.
     */
    val workingDirectory: File?

    /**
     * Resolves a local file path, either absolutely or relatively from [workingDirectory].
     * This does not perform any check for file existence.
     * @param path absolute or relative file path to resolve
     * @return the resolved file
     */
    fun resolve(path: String): File
}

/**
 * A simple [FileSystem] implementation that resolves paths
 * based on an optional working directory.
 */
data class SimpleFileSystem(
    override val workingDirectory: File? = null,
) : FileSystem {
    override fun resolve(path: String): File = IOUtils.resolvePath(path, workingDirectory)
}
