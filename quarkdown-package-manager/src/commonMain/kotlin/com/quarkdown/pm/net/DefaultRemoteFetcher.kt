package com.quarkdown.pm.net

import io.ktor.http.Url

/**
 * The [RemoteFetcher] used when none is supplied to a package repository.
 * Platforms that cannot download synchronously raise
 * [com.quarkdown.core.platform.UnsupportedPlatformOperationException] on fetch;
 * embedders there must inject their own fetcher.
 */
expect object DefaultRemoteFetcher : RemoteFetcher {
    override fun fetchBytes(url: Url): ByteArray
}
