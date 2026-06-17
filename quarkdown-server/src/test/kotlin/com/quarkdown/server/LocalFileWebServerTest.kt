package com.quarkdown.server

import com.quarkdown.server.stop.Stoppable
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.sse.SSE
import io.ktor.client.plugins.sse.sse
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.file.Files
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Tests for [LocalFileWebServer].
 */
class LocalFileWebServerTest {
    private lateinit var tempDir: File
    private lateinit var testFile: File
    private lateinit var server: LocalFileWebServer
    private var port: Int = 0
    private var serverStoppable: Stoppable? = null

    private val baseUrl: String get() = "http://$SERVER_HOST:$port"

    @BeforeTest
    fun setUp() {
        // Create a temporary directory
        tempDir = Files.createTempDirectory("webserver-test").toFile()
        tempDir.deleteOnExit()

        // Create a test file
        testFile = File(tempDir, "test.html")
        testFile.writeText(
            """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Test Page</title>
            </head>
            <body>
                <h1>Test Page</h1>
                <p>This is a test page for the LocalFileWebServer.</p>
            </body>
            </html>
            """.trimIndent(),
        )

        // Create server
        server = LocalFileWebServer(testFile)

        // Find a free port
        port = findFreePort()

        // Start server in a separate thread
        val latch = CountDownLatch(1)
        thread {
            server.start(port) { stoppable ->
                serverStoppable = stoppable
                latch.countDown()
            }
        }

        // Wait for server to start
        assertTrue(latch.await(5, TimeUnit.SECONDS), "Server did not start within timeout")
    }

    @AfterTest
    fun tearDown() {
        // Stop server
        serverStoppable?.stop()

        // Clean up temp directory
        tempDir.deleteRecursively()
    }

    @Test
    fun `server serves file`() =
        runBlocking {
            val client = HttpClient(CIO)

            client.use { client ->
                val response = client.get("$baseUrl/")

                assertEquals(HttpStatusCode.OK, response.status)
                val responseText = response.bodyAsText()
                assertTrue(responseText.contains("<title>Test Page</title>"))
                assertTrue(responseText.contains("<h1>Test Page</h1>"))
            }
        }

    @Test
    fun `reload event is delivered to subscriber`() =
        runBlocking {
            val client =
                HttpClient(CIO) {
                    install(SSE)
                }

            client.use { client ->
                val received = CompletableDeferred<String>()

                val subscriber =
                    launch {
                        client.sse("$baseUrl/reload") {
                            val event = incoming.first()
                            received.complete(event.data ?: "")
                        }
                    }

                // Give the subscription time to settle.
                delay(500)

                val triggerStatus = client.post("$baseUrl/reload").status

                val payload = withTimeout(5.seconds) { received.await() }

                assertEquals(HttpStatusCode.NoContent, triggerStatus)
                assertEquals("reload", payload, "Subscriber should receive the broadcast reload event")

                subscriber.cancel()
            }
        }

    @Test
    fun `multiple subscribers receive the same reload broadcasts`() =
        runBlocking {
            val client =
                HttpClient(CIO) {
                    install(SSE)
                }

            client.use { client ->
                val numSubscribers = 5
                val numBroadcasts = 5
                val receivedTotal = AtomicInteger(0)
                val allReceived = CompletableDeferred<Int>()

                val subscriberJobs =
                    List(numSubscribers) {
                        launch {
                            client.sse("$baseUrl/reload") {
                                try {
                                    var count = 0
                                    incoming.collect {
                                        count++
                                        val total = receivedTotal.incrementAndGet()
                                        if (total >= numSubscribers * numBroadcasts) {
                                            allReceived.complete(total)
                                        }
                                        if (count >= numBroadcasts) return@collect
                                    }
                                } catch (_: CancellationException) {
                                    // Expected when the test completes.
                                }
                            }
                        }
                    }

                // Give all subscribers time to connect.
                delay(500)

                repeat(numBroadcasts) {
                    client.post("$baseUrl/reload")
                }

                val total =
                    withTimeout(10.seconds) {
                        allReceived.await()
                    }

                assertEquals(
                    numSubscribers * numBroadcasts,
                    total,
                    "Every subscriber should receive every broadcast",
                )

                subscriberJobs.forEach { it.cancel() }
            }
        }

    @Test
    fun `late-connecting subscriber does not receive stale broadcasts`() =
        runBlocking {
            val client =
                HttpClient(CIO) {
                    install(SSE)
                }

            client.use { client ->
                // Broadcast before any subscriber is listening.
                client.post("$baseUrl/reload")
                delay(300)

                // A subscriber connecting now should not see the earlier broadcast.
                val receivedStale = CompletableDeferred<Boolean>()
                val lateSubscriber =
                    launch {
                        client.sse("$baseUrl/reload") {
                            incoming.first()
                            receivedStale.complete(true)
                        }
                    }

                // Give the late subscriber a window to (incorrectly) receive a stale broadcast.
                // The job is then cancelled so the SSE session is torn down regardless.
                val gotStale =
                    withTimeoutOrNull(1.seconds) { receivedStale.await() } ?: false
                lateSubscriber.cancel()

                assertEquals(false, gotStale, "Late-connecting subscriber should not receive stale broadcasts")
            }
        }

    @Test
    fun `abruptly disconnected subscriber does not break the server`() =
        runBlocking {
            // Open a raw TCP connection and start an SSE subscription, then force a TCP RST (rather than a graceful FIN) by closing with SO_LINGER(0).
            Socket(SERVER_HOST, port).use { socket ->
                val out = socket.getOutputStream()
                out.write(
                    (
                        "GET ${ServerEndpoints.RELOAD_LIVE_PREVIEW} HTTP/1.1\r\n" +
                            "Host: $SERVER_HOST:$port\r\n" +
                            "Accept: text/event-stream\r\n" +
                            "Connection: keep-alive\r\n" +
                            "\r\n"
                    ).toByteArray(),
                )
                out.flush()

                // Wait for the end of HTTP headers so we know the server has registered the subscription.
                // Bound the read so a stalled handshake fails the test fast instead of hanging the suite.
                socket.soTimeout = 2_000
                val reader = socket.getInputStream().bufferedReader()
                try {
                    while (true) {
                        val line = reader.readLine() ?: break
                        if (line.isEmpty()) break
                    }
                } catch (_: SocketTimeoutException) {
                    // Treat as "headers stalled": proceed with the abrupt close anyway,
                    // which is what the test is here to exercise.
                }

                // Force an immediate RST instead of a graceful close.
                socket.setSoLinger(true, 0)
            }

            // Give the server a moment to either notice the disconnect or queue a write to the dead socket.
            delay(200)

            HttpClient(CIO) {
                install(SSE)
            }.use { client ->
                // Server must still respond to triggers after the abrupt disconnect.
                val triggerStatus = client.post("$baseUrl/reload").status
                assertEquals(HttpStatusCode.NoContent, triggerStatus)

                // A fresh subscriber must still receive subsequent broadcasts, proving the broadcast
                // flow was not poisoned by the failed write to the dead subscriber.
                val received = CompletableDeferred<String>()
                val subscriber =
                    launch {
                        client.sse("$baseUrl/reload") {
                            val event = incoming.first()
                            received.complete(event.data ?: "")
                        }
                    }
                delay(500)
                client.post("$baseUrl/reload")

                val payload = withTimeout(5.seconds) { received.await() }
                assertEquals("reload", payload)
                subscriber.cancel()
            }
        }

    @Test
    fun `server handles file not found`() =
        runBlocking {
            val client = HttpClient(CIO)

            client.use { client ->
                val response = client.get("$baseUrl/nonexistent.html")

                assertEquals(HttpStatusCode.NotFound, response.status)
            }
        }

    // Helper method to find a free port
    private fun findFreePort(): Int =
        ServerFreePortScanner(
            object : Server {
                override fun start(
                    port: Int,
                    wait: Boolean,
                    onReady: (Stoppable) -> Unit,
                ) {
                    // Do nothing, just testing if port is available
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
