package com.quarkdown.pm.net

import com.sun.net.httpserver.HttpServer
import io.ktor.http.Url
import java.net.InetSocketAddress
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteExisting
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIsNot
import kotlin.test.assertTrue

/**
 * Tests for the JVM [DefaultRemoteFetcher].
 */
class DefaultRemoteFetcherTest {
    @Test
    fun `reads bytes and text from a file url`() {
        val file = createTempFile(suffix = ".qd")
        try {
            file.writeText("# Hello")
            val url = Url(file.toUri().toString())
            assertEquals("# Hello", DefaultRemoteFetcher.fetchText(url))
            assertEquals("# Hello".encodeToByteArray().toList(), DefaultRemoteFetcher.fetchBytes(url).toList())
        } finally {
            file.deleteExisting()
        }
    }

    @Test
    fun `missing resource raises not found`() {
        val file = createTempFile(suffix = ".qd")
        file.deleteExisting()
        val url = Url(file.toUri().toString())
        val exception = assertFailsWith<RemoteFetchException.NotFound> { DefaultRemoteFetcher.fetchBytes(url) }
        assertEquals(url, exception.url)
    }

    @Test
    fun `malformed url raises fetch exception`() {
        val url = Url("file:///no such dir/x.qd")
        val exception = assertFailsWith<RemoteFetchException> { DefaultRemoteFetcher.fetchBytes(url) }
        assertEquals(url, exception.url)
    }

    @Test
    fun `stalled response fails within the read timeout`() =
        withHttpServer { baseUrl ->
            val fetcher = UrlRemoteFetcher(connectTimeoutMillis = 1_000, readTimeoutMillis = 200)
            val started = System.nanoTime()
            val failure = assertFailsWith<RemoteFetchException> { fetcher.fetchBytes(Url("$baseUrl/stalled")) }
            assertIsNot<RemoteFetchException.NotFound>(failure)
            assertTrue((System.nanoTime() - started) / 1_000_000 < 900, "timed out too late")
        }

    @Test
    fun `http 404 raises not found and other errors do not`() =
        withHttpServer { baseUrl ->
            assertFailsWith<RemoteFetchException.NotFound> { DefaultRemoteFetcher.fetchBytes(Url("$baseUrl/missing")) }
            val failure = assertFailsWith<RemoteFetchException> { DefaultRemoteFetcher.fetchBytes(Url("$baseUrl/broken")) }
            assertIsNot<RemoteFetchException.NotFound>(failure)
            assertEquals("ok", DefaultRemoteFetcher.fetchText(Url("$baseUrl/present")))
        }

    private fun withHttpServer(block: (baseUrl: String) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/") { exchange ->
            val (status, body) =
                when (exchange.requestURI.path) {
                    "/stalled" -> {
                        Thread.sleep(1_000)
                        200 to "late"
                    }
                    "/present" -> 200 to "ok"
                    "/broken" -> 500 to "boom"
                    else -> 404 to "nope"
                }
            val bytes = body.encodeToByteArray()
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        try {
            block("http://127.0.0.1:${server.address.port}")
        } finally {
            server.stop(0)
        }
    }
}
