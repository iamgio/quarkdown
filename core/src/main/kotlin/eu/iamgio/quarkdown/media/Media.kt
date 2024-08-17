package eu.iamgio.quarkdown.media

import eu.iamgio.quarkdown.util.toURLOrNull
import java.io.File
import java.net.URL

/**
 * Represents a resource that can be referenced in a Quarkdown document
 * and that may need to be downloaded or processed.
 * For example, when exporting a document to HTML, remote images are handled by the browser,
 * while local ones need to be copied to the output resources.
 */
interface Media {
    companion object {
        /**
         * @param path path to media, either a file or a URL
         * @return [LocalMedia] if the path is a file, [RemoteMedia] if the path is a URL
         * @throws IllegalArgumentException if the path cannot be resolved or if it is a directory
         */
        fun of(path: String): Media {
            // If the path is a URL, it is remote.
            path.toURLOrNull()?.let { return RemoteMedia(it) }

            val file = File(path)

            if (!file.exists()) throw IllegalArgumentException("Media path cannot be resolved: $path")
            if (file.isDirectory) throw IllegalArgumentException("Media is a directory: $path")

            return LocalMedia(file)
        }
    }
}

/**
 * A media that lives on the local filesystem.
 * @param file the local file where the media is stored
 */
data class LocalMedia(val file: File) : Media

/**
 * A media stored remotely.
 * @param url the URL where the media is stored
 */
data class RemoteMedia(val url: URL) : Media
