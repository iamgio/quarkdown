package eu.iamgio.quarkdown.media.export

import eu.iamgio.quarkdown.media.LocalMedia
import eu.iamgio.quarkdown.media.Media
import eu.iamgio.quarkdown.media.MediaVisitor
import eu.iamgio.quarkdown.media.RemoteMedia
import eu.iamgio.quarkdown.pipeline.output.ArtifactType
import eu.iamgio.quarkdown.pipeline.output.BinaryOutputArtifact
import eu.iamgio.quarkdown.pipeline.output.OutputResource

/**
 * A converter of a [Media] to an [OutputResource].
 * @param name generated media name
 */
class MediaOutputResourceConverter(private val name: String) : MediaVisitor<OutputResource> {
    override fun visit(media: LocalMedia) =
        BinaryOutputArtifact(
            name,
            media.file.readBytes(),
            ArtifactType.AUTO,
        )

    override fun visit(media: RemoteMedia) =
        BinaryOutputArtifact(
            name,
            media.url.openStream().readBytes(),
            ArtifactType.AUTO,
        )
}
