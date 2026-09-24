package com.quarkdown.core.media.fetch

import com.quarkdown.core.media.RemoteMedia

/**
 * Strategy for downloading the content of a [RemoteMedia], used when the media storage
 * materializes remote media into the output resources.
 * @see DefaultRemoteMediaFetcher for the platform default
 */
fun interface RemoteMediaFetcher {
    /**
     * Downloads the content of [media]. Blocking call.
     * @param media the remote media to fetch
     * @return the raw bytes of the downloaded content
     * @throws com.quarkdown.core.platform.UnsupportedPlatformOperationException on platforms that cannot download synchronously
     */
    fun fetch(media: RemoteMedia): ByteArray
}
