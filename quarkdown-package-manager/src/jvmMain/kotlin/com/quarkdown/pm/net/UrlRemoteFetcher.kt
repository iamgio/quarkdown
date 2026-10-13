package com.quarkdown.pm.net

import io.ktor.http.Url
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

/**
 * [RemoteFetcher] that downloads content by opening a URL connection with finite timeouts,
 * so an unreachable or stalled server fails the fetch instead of hanging a compilation.
 * A [FileNotFoundException], which the JDK raises both for missing local files and for HTTP 404 responses,
 * is reported as [RemoteFetchException.NotFound]; any other I/O or URL failure as [RemoteFetchException].
 * @param connectTimeoutMillis maximum time to establish the connection
 * @param readTimeoutMillis maximum time to wait for data once connected
 */
class UrlRemoteFetcher(
    private val connectTimeoutMillis: Int = DEFAULT_CONNECT_TIMEOUT_MILLIS,
    private val readTimeoutMillis: Int = DEFAULT_READ_TIMEOUT_MILLIS,
) : RemoteFetcher {
    override fun fetchBytes(url: Url): ByteArray =
        try {
            URI(url.toString())
                .toURL()
                .openConnection()
                .apply {
                    connectTimeout = connectTimeoutMillis
                    readTimeout = readTimeoutMillis
                }.getInputStream()
                .use { it.readBytes() }
        } catch (e: FileNotFoundException) {
            throw RemoteFetchException.NotFound(url, e)
        } catch (e: IOException) {
            throw RemoteFetchException(url, e.message ?: e::class.simpleName.orEmpty(), e)
        } catch (e: URISyntaxException) {
            throw RemoteFetchException(url, e.message ?: "malformed URL", e)
        } catch (e: IllegalArgumentException) {
            throw RemoteFetchException(url, e.message ?: "malformed URL", e)
        }

    companion object {
        /**
         * Connect timeout used by [DefaultRemoteFetcher].
         */
        const val DEFAULT_CONNECT_TIMEOUT_MILLIS = 15_000

        /**
         * Read timeout used by [DefaultRemoteFetcher].
         */
        const val DEFAULT_READ_TIMEOUT_MILLIS = 60_000
    }
}
