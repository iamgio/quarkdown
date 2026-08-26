package com.quarkdown.core.media.export

import com.quarkdown.core.media.LocalMedia
import com.quarkdown.core.media.Media
import com.quarkdown.core.media.MediaVisitor
import com.quarkdown.core.media.RemoteMedia
import com.quarkdown.core.pipeline.output.ArtifactType
import com.quarkdown.core.pipeline.output.BinaryOutputArtifact
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.output.toOutputResource

/**
 * A converter of a [Media] to an [OutputResource].
 * @param name generated media name
 */
class MediaOutputResourceConverter(
    private val name: String,
) : MediaVisitor<OutputResource> {
    // Disk-backed media is copied efficiently by reference; virtual media is materialized in memory.
    override fun visit(media: LocalMedia) = media.file.toOutputResource(name, useChecksumInvalidation = true)

    override fun visit(media: RemoteMedia) =
        BinaryOutputArtifact(
            name,
            media.url
                .openStream()
                .readBytes()
                .toList(),
            ArtifactType.AUTO,
        )
}
