package com.quarkdown.pm.net

import io.ktor.http.Url

/**
 * In-memory [RemoteFetcher] that serves canned responses keyed by URL string
 * and records every request, for tests.
 * @param responses response body by URL string
 */
class FakeRemoteFetcher(
    private val responses: Map<String, ByteArray>,
) : RemoteFetcher {
    /**
     * URLs requested so far, in order.
     */
    val requested = mutableListOf<Url>()

    override fun fetchBytes(url: Url): ByteArray {
        requested += url
        return responses[url.toString()] ?: throw RemoteFetchException.NotFound(url)
    }

    companion object {
        /**
         * @return a fetcher serving [responses], given as text
         */
        fun ofText(responses: Map<String, String>) = FakeRemoteFetcher(responses.mapValues { it.value.encodeToByteArray() })
    }
}
