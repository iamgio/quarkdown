package com.quarkdown.pm.net

import io.ktor.http.Url

/**
 * Downloads content synchronously through a [UrlRemoteFetcher] with its default timeouts.
 */
actual object DefaultRemoteFetcher : RemoteFetcher {
    private val delegate = UrlRemoteFetcher()

    actual override fun fetchBytes(url: Url): ByteArray = delegate.fetchBytes(url)
}
