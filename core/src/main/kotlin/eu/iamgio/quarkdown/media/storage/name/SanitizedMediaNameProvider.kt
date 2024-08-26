package eu.iamgio.quarkdown.media.storage.name

import eu.iamgio.quarkdown.media.LocalMedia
import eu.iamgio.quarkdown.media.RemoteMedia

/**
 *
 */
class SanitizedMediaNameProvider : MediaNameProviderStrategy {
    private fun String.sanitize() = this.replace("\\W".toRegex(), "-")

    override fun visit(media: LocalMedia) = media.file.absolutePath.sanitize()

    override fun visit(media: RemoteMedia) = media.url.path.sanitize()
}
