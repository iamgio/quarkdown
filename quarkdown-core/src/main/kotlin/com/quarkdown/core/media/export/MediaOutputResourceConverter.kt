package com.quarkdown.core.media.export

import com.quarkdown.core.media.LocalMedia
import com.quarkdown.core.media.Media
import com.quarkdown.core.media.MediaVisitor
import com.quarkdown.core.media.RemoteMedia
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource

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
