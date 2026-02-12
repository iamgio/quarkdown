package com.quarkdown.server

import com.quarkdown.server.stop.Stoppable
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the live preview endpoint (`/live/{file...}`).
 *
 * Unlike [LocalFileWebServerTest], the server is backed by a **directory**
 * so that subdirectory file resolution and static file serving work correctly.
 */
class LivePreviewEndpointTest {
    private lateinit var tempDir: File
    private lateinit var server: LocalFileWebServer
    private var port: Int = 0
    private var serverStoppable: Stoppable? = null

    @BeforeTest
    fun setUp() {
        tempDir = Files.createTempDirectory("live-preview-test").toFile()
        tempDir.deleteOnExit()

        // Create a root HTML file.
        File(tempDir, "test.html").writeText("<html><body>Root</body></html>")

        // The server is backed by the directory.
        server = LocalFileWebServer(tempDir)

        port = findFreePort()

        val latch = CountDownLatch(1)
        thread {
            server.start(port) { stoppable ->
                serverStoppable = stoppable
                latch.countDown()
            }
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Server did not start within timeout")
    }

    @AfterTest
    fun tearDown() {
        serverStoppable?.stop()
        tempDir.deleteRecursively()
    }

    @Test
    fun `live preview serves HTML wrapper for root file`() =
        runBlocking {
            val client = HttpClient(CIO)
            client.use { client ->
                val response = client.get("http://localhost:$port/live/test.html")
                assertEquals(HttpStatusCode.OK, response.status)

                val body = response.bodyAsText()
                // The wrapper must contain an iframe whose src is an absolute path from the root.
                assertTrue(body.contains("src=\"/test.html\""), "Expected iframe src=\"/test.html\" in wrapper")
                // The wrapper must include the WebSocket script with the correct server port.
                assertTrue(body.contains("localhost:$port"), "Expected server port in WebSocket script")
            }
        }

    @Test
    fun `live preview serves HTML wrapper for subdirectory file`() =
        runBlocking {
            // Create a file inside a subdirectory.
            val subdir = File(tempDir, "subdir").apply { mkdir() }
            File(subdir, "page.html").writeText("<html><body>Sub</body></html>")

            val client = HttpClient(CIO)
            client.use { client ->
                val response = client.get("http://localhost:$port/live/subdir/page.html")
                assertEquals(HttpStatusCode.OK, response.status)

                val body = response.bodyAsText()
                // The iframe src must use an absolute path that includes the subdirectory.
                assertTrue(
                    body.contains("src=\"/subdir/page.html\""),
                    "Expected iframe src=\"/subdir/page.html\" in wrapper",
                )
            }
        }

    @Test
    fun `live preview serves non-HTML files directly`() =
        runBlocking {
            val cssContent = "body { color: red; }"
            File(tempDir, "style.css").writeText(cssContent)

            val client = HttpClient(CIO)
            client.use { client ->
                val response = client.get("http://localhost:$port/live/style.css")
                assertEquals(HttpStatusCode.OK, response.status)

                val body = response.bodyAsText()
                assertEquals(cssContent, body)
            }
        }

    @Test
    fun `live preview returns 404 for non-existent file`() =
        runBlocking {
            val client = HttpClient(CIO)
            client.use { client ->
                val response = client.get("http://localhost:$port/live/nonexistent.html")
                assertEquals(HttpStatusCode.NotFound, response.status)

                val body = response.bodyAsText()
                // The endpoint must respond with a human-readable "Not Found" body.
                assertEquals("Not Found", body)
            }
        }

    /**
     * Finds a free port by using [ServerFreePortScanner] with a no-op server.
     */
    private fun findFreePort(): Int =
        ServerFreePortScanner(
            object : Server {
                override fun start(
                    port: Int,
                    wait: Boolean,
                    onReady: (Stoppable) -> Unit,
                ) {
                    onReady(
                        object : Stoppable {
                            override fun stop() {}
                        },
                    )
                }
            },
        ).run {
            val portDeferred = CompletableDeferred<Int>()
            attemptStartUntilPortAvailable(8000) { _, port ->
                portDeferred.complete(port)
            }
            runBlocking { portDeferred.await() }
        }
}
