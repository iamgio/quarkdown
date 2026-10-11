package com.quarkdown.pm.net

import io.ktor.http.Url
import java.io.FileNotFoundException
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

/**
 * Downloads content synchronously by opening a stream on the URL.
 * A [FileNotFoundException], which the JDK raises both for missing local files and for HTTP 404 responses,
 * is reported as [RemoteFetchException.NotFound]; any other I/O or URL failure as [RemoteFetchException].
 */
actual object DefaultRemoteFetcher : RemoteFetcher {
    actual override fun fetchBytes(url: Url): ByteArray =
        try {
            URI(url.toString()).toURL().openStream().use { it.readBytes() }
        } catch (e: FileNotFoundException) {
            throw RemoteFetchException.NotFound(url, e)
        } catch (e: IOException) {
            throw RemoteFetchException(url, e.message ?: e::class.simpleName.orEmpty(), e)
        } catch (e: URISyntaxException) {
            throw RemoteFetchException(url, e.message ?: "malformed URL", e)
        } catch (e: IllegalArgumentException) {
            throw RemoteFetchException(url, e.message ?: "malformed URL", e)
        }
}
