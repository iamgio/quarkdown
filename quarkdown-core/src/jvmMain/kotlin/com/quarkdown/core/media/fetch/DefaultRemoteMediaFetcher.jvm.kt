package com.quarkdown.core.media.fetch

import com.quarkdown.core.media.RemoteMedia
import java.net.URI

/**
 * Downloads content synchronously by opening a stream on the media's URL.
 */
actual object DefaultRemoteMediaFetcher : RemoteMediaFetcher {
    actual override fun fetch(media: RemoteMedia): ByteArray =
        URI(media.url.toString())
            .toURL()
            .openStream()
            .use { it.readBytes() }
}
