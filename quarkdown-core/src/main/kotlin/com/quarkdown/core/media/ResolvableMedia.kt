package com.quarkdown.core.media

import com.quarkdown.core.filesystem.FileSystem
import com.quarkdown.core.util.toURLOrNull

/**
 * A generic media that is yet to be resolved to a [Media] subclass.
 * @param path path to the media, either a file or a URL
 * @param fileSystem file system to resolve the media from, in case the path is a local file
 */
data class ResolvableMedia(
    private val path: String,
    private val fileSystem: FileSystem,
) : Media {
    /**
     * The resolved media as a [LocalMedia] or [RemoteMedia].
     */
    private val resolved: Media by lazy(::resolve)

    /**
     * @return [LocalMedia] if the path is a file, [RemoteMedia] if the path is a URL
     * @throws IllegalArgumentException if the path cannot be resolved or if it is a directory
     */
    private fun resolve(): Media {
        // If the path is a URL, it is remote.
        path.toURLOrNull()?.let { return RemoteMedia(it) }

        val file = fileSystem.resolve(path)

        if (!file.exists) throw IllegalArgumentException("Media path cannot be resolved: $path")
        if (file.isDirectory) throw IllegalArgumentException("Media is a directory: $path")

        return LocalMedia(file)
    }

    // Delegate to the resolved media.
    override fun <T> accept(visitor: MediaVisitor<T>): T = resolved.accept(visitor)
}
