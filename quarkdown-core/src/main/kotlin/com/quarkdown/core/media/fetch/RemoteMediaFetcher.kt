package com.quarkdown.core.media.fetch

import com.quarkdown.core.media.RemoteMedia

/**
 * Strategy for downloading the content of a [RemoteMedia], used when the media storage
 * materializes remote media into the output resources.
 * @see UrlRemoteMediaFetcher for a default implementation that downloads content synchronously
 */
fun interface RemoteMediaFetcher {
    /**
     * Downloads the content of [media]. Blocking call.
     * @param media the remote media to fetch
     * @return the raw bytes of the downloaded content
     * @throws java.io.IOException if the content cannot be retrieved
     */
    fun fetch(media: RemoteMedia): ByteArray
}
