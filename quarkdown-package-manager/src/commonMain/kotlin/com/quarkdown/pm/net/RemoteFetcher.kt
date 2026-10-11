package com.quarkdown.pm.net

import io.ktor.http.Url

/**
 * Strategy for downloading remote content. Calls are blocking.
 *
 * Package repositories receive a fetcher at construction so that platforms without synchronous networking,
 * such as browsers, can supply one backed by pre-fetched content.
 * @see DefaultRemoteFetcher for the platform default
 */
fun interface RemoteFetcher {
    /**
     * Downloads the content at [url].
     * @param url resource to download
     * @return raw bytes of the resource
     * @throws RemoteFetchException if the resource cannot be retrieved
     * @throws RemoteFetchException.NotFound if the resource does not exist
     */
    fun fetchBytes(url: Url): ByteArray

    /**
     * Downloads the content at [url] and decodes it as UTF-8 text.
     * @param url resource to download
     * @return text content of the resource
     * @throws RemoteFetchException if the resource cannot be retrieved
     * @throws RemoteFetchException.NotFound if the resource does not exist
     */
    fun fetchText(url: Url): String = fetchBytes(url).decodeToString()
}
