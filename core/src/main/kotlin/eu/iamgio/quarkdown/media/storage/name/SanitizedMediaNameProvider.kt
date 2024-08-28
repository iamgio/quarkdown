package eu.iamgio.quarkdown.media.storage.name

import eu.iamgio.quarkdown.media.LocalMedia
import eu.iamgio.quarkdown.media.RemoteMedia
import eu.iamgio.quarkdown.util.sanitizeFileName

/**
 * A media name generator that sanitizes the file name and includes a unique identifier in it.
 * For example, "path/to/my file.jpg" is mapped to "my-file@HASH.jpg"
 */
class SanitizedMediaNameProvider : MediaNameProviderStrategy {
    private fun String.sanitize() = this.sanitizeFileName(replacement = "-")

    // Local media are given a unique identifier based on their file name and hash code.
    override fun visit(media: LocalMedia) =
        buildString {
            append(media.file.nameWithoutExtension)
            append("@")
            append(media.file.hashCode())
            append(".")
            append(media.file.extension)
        }.sanitize()

    // URLs are already unique, and they don't need an additional identifier.
    override fun visit(media: RemoteMedia) = media.url.toExternalForm().sanitize()
}
