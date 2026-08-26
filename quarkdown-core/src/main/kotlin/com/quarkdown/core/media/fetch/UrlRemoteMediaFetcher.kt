package com.quarkdown.core.media.fetch

import com.quarkdown.core.media.RemoteMedia

/**
 * Default [RemoteMediaFetcher] that downloads content synchronously
 * by opening a stream on the media's URL.
 */
object UrlRemoteMediaFetcher : RemoteMediaFetcher {
    override fun fetch(media: RemoteMedia): ByteArray = media.url.openStream().use { it.readBytes() }
}
