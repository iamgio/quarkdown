package eu.iamgio.quarkdown.media.storage.name

import eu.iamgio.quarkdown.media.LocalMedia
import eu.iamgio.quarkdown.media.RemoteMedia
import eu.iamgio.quarkdown.util.sanitize
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * A media name generator that sanitizes the file name.
 * For example, "my file.jpg" is mapped to "<parent>-my-file.jpg"
 */
@OptIn(ExperimentalEncodingApi::class)
class SanitizedMediaNameProvider : MediaNameProviderStrategy {
    private fun String.sanitize() = this.sanitize(replacement = "-")

    override fun visit(media: LocalMedia) =
        buildString {
            append(media.file.nameWithoutExtension)
            append("-")
            append(Base64.UrlSafe.encode(media.file.canonicalPath.toByteArray()))
            append(".")
            append(media.file.extension)
        }.sanitize()

    override fun visit(media: RemoteMedia) = media.url.toExternalForm().sanitize()
}
