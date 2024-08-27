package eu.iamgio.quarkdown.media.storage.name

import eu.iamgio.quarkdown.media.LocalMedia
import eu.iamgio.quarkdown.media.RemoteMedia

/**
 *
 */
class SanitizedMediaNameProvider : MediaNameProviderStrategy {
    // Must preserve the file extension
    private fun String.sanitize() = replace("[^a-zA-Z0-9\\-_.]".toRegex(), "-").trim()

    override fun visit(media: LocalMedia) = media.file.canonicalPath.sanitize()

    override fun visit(media: RemoteMedia) = media.url.path.sanitize()
}
