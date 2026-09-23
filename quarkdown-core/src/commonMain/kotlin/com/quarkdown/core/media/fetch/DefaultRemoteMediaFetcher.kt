package com.quarkdown.core.media.fetch

import com.quarkdown.core.media.RemoteMedia

/**
 * The [RemoteMediaFetcher] used when none is supplied through the media storage options.
 * Platforms that cannot download synchronously raise
 * [com.quarkdown.core.platform.UnsupportedPlatformOperationException] on [fetch];
 * embedders there must inject their own fetcher backed by pre-downloaded content.
 */
expect object DefaultRemoteMediaFetcher : RemoteMediaFetcher {
    override fun fetch(media: RemoteMedia): ByteArray
}
