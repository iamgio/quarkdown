package com.quarkdown.server

import com.quarkdown.server.stop.Stoppable
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
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
            // Create HTTP client
            val client = HttpClient(CIO)

            try {
                // Request the file
                val response = client.get("http://localhost:$port/")

                // Check response
                assertEquals(HttpStatusCode.OK, response.status)
                val responseText = response.bodyAsText()
                assertTrue(responseText.contains("<title>Test Page</title>"))
                assertTrue(responseText.contains("<h1>Test Page</h1>"))
            } finally {
                client.close()
            }
        }

    @Test
    fun `websocket reload`() =
        runBlocking {
            // Create HTTP client with WebSockets support
            val client =
                HttpClient(CIO) {
                    install(WebSockets)
                }

            try {
                // Connect to reload endpoint
                val messageReceived = CompletableDeferred<String>()

                // First client to receive reload messages
                val receiverJob =
                    launch {
                        client.webSocket("ws://localhost:$port/reload") {
                            // Wait for a message
                            val frame = incoming.receive()
                            if (frame is Frame.Text) {
                                messageReceived.complete(frame.readText())
                            }
                        }
                    }

                // Wait a bit to ensure connection is established
                delay(500)

                // Second client to send reload message
                val senderJob =
                    launch {
                        client.webSocket("ws://localhost:$port/reload") {
                            // Send a reload message
                            send(Frame.Text("reload"))
                        }
                    }

                // Wait for message to be received with timeout
                val receivedMessage =
                    withTimeout(5.seconds) {
                        messageReceived.await()
                    }

                // Check received message
                assertEquals("reload", receivedMessage)

                // Cancel jobs
                receiverJob.cancel()
                senderJob.cancel()
            } finally {
                client.close()
            }
        }

    @Test
    fun `concurrent reload requests`() =
        runBlocking {
            // Create HTTP client with WebSockets support
            val client =
                HttpClient(CIO) {
                    install(WebSockets)
                }

            try {
                // Connect multiple clients and send reload messages concurrently
                val numClients = 5
                val messagesReceived = CompletableDeferred<Int>()
                var receivedCount = 0

                // Start a receiver to count messages
                val receiverJob =
                    launch {
                        client.webSocket("ws://localhost:$port/reload") {
                            try {
                                repeat(numClients) {
                                    val frame = incoming.receive()
                                    if (frame is Frame.Text) {
                                        receivedCount++
                                        if (receivedCount >= numClients) {
                                            messagesReceived.complete(receivedCount)
                                        }
                                    }
                                }
                            } catch (_: CancellationException) {
                                // Expected when job is cancelled
                            } catch (e: Exception) {
                                println("[DEBUG_LOG] Error in receiver: ${e.message}")
                            }
                        }
                    }

                // Wait a bit to ensure connection is established
                delay(500)

                // Launch multiple senders concurrently
                val senderJobs =
                    List(numClients) { clientId ->
                        launch {
                            client.webSocket("ws://localhost:$port/reload") {
                                // Send a reload message
                                send(Frame.Text("reload-$clientId"))
                            }
                        }
                    }

                // Wait for all messages to be received with timeout
                val count =
                    withTimeout(10.seconds) {
                        messagesReceived.await()
                    }

                // Check received message count
                assertEquals(numClients, count)

                // Cancel jobs
                receiverJob.cancel()
                senderJobs.forEach { it.cancel() }
            } finally {
                client.close()
            }
        }

    @Test
    fun `server handles file not found`() =
        runBlocking {
            // Create HTTP client
            val client = HttpClient(CIO)

            try {
                // Request a non-existent file
                val response = client.get("http://localhost:$port/nonexistent.html")

                // Check response - should be 404 Not Found
                assertEquals(HttpStatusCode.NotFound, response.status)
            } finally {
                client.close()
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
