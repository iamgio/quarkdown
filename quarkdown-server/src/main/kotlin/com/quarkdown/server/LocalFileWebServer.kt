package com.quarkdown.server

import com.quarkdown.server.stop.KtorStoppableAdapter
import com.quarkdown.server.stop.Stoppable
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ServerReady
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

/**
 * Web server that:
 * - Serves local file at [targetFile];
 * - Supports live preview of HTML files at `/live/{file...}`.
 * - Supports live-reloading via WebSockets at `/reload`.
 * @param targetFile file to serve
 */
class LocalFileWebServer(
    private val targetFile: File,
) : Server {
    // Trackers of active connections.
    private val activeConnections = ConcurrentHashMap<String, Boolean>()
    private val connectionCounter = AtomicInteger(0)

    /**
     * Starts the server on [port].
     * @throws IllegalArgumentException if [targetFile] does not exist
     */
    override fun start(
        port: Int,
        wait: Boolean,
        onReady: (Stoppable) -> Unit,
    ) {
        if (!targetFile.exists()) {
            throw IllegalArgumentException("Cannot start web server for non-existing file: $targetFile")
        }

        // Shared flow to broadcast messages to all connected clients with replay capability
        // This ensures that clients connecting after a message is sent will still receive it
        // Using a larger replay buffer to ensure all messages are delivered in concurrent scenarios
        val messageResponseFlow =
            MutableSharedFlow<String>(
                replay = 10,
                extraBufferCapacity = 10,
            )
        val sharedFlow = messageResponseFlow.asSharedFlow()

        embeddedServer(Netty, port) {
            install(WebSockets) {
                pingPeriod = 10.seconds
                timeout = 15.seconds
                maxFrameSize = Long.MAX_VALUE
            }

            monitor.subscribe(ServerReady) { onReady(KtorStoppableAdapter(this)) }

            routing {
                // Serves the target file directly at the root path.
                staticFiles(ServerEndpoints.ROOT, targetFile)

                // Endpoint to serve files HTML files with WebSockets-based live preview support.
                get(ServerEndpoints.LIVE_PREVIEW + "/{file...}") {
                    val segments = call.parameters.getAll("file")?.takeIf { it.isNotEmpty() } ?: listOf("index.html")
                    val path = segments.joinToString("/") // e.g. file.html or subdir/file.html
                    val file = targetFile.resolve(path)

                    if (!file.exists() || !file.isFile) {
                        call.respond(HttpStatusCode.NotFound, null)
                        return@get
                    }

                    if (file.extension.lowercase() == "html") {
                        val websocketsScriptContent = javaClass.getResource("/script/websockets.js")!!.readText()

                        // Since we are one level deep in /live/, we need to adjust the relative path to the source file.
                        val sourceFile = "../${file.name}"

                        val style =
                            "position: fixed; top: 0; left: 0; width: 100%; height: 100%; border: none;" +
                                "margin: 0; padding: 0; overflow: hidden; z-index: 999999;"
                        val html =
                            """
                            <!DOCTYPE html>
                            <html>
                            <head>
                            </head>
                            <body>
                            <iframe id="content-frame" src="$sourceFile" style="$style"></iframe>
                            <script>
                            $websocketsScriptContent
                            
                            startWebSockets("localhost:$port")
                            </script>
                            </body>
                            """.trimIndent()
                        call.respondText(html, ContentType.Text.Html)
                    } else if (file.exists()) {
                        // For non-HTML files.
                        call.respondFile(file)
                    }
                }

                // WebSocket endpoint to reload live preview clients.
                webSocket(ServerEndpoints.RELOAD_LIVE_PREVIEW) {
                    val connectionId = "connection-${connectionCounter.incrementAndGet()}"

                    try {
                        activeConnections[connectionId] = true
                        log.info("WebSocket connection established: $connectionId")

                        // Forward messages to this client.
                        launch {
                            try {
                                sharedFlow.collect { message ->
                                    if (activeConnections.containsKey(connectionId)) {
                                        try {
                                            send(Frame.Text(message))
                                            log.debug("Sent message to $connectionId: $message")
                                        } catch (e: Exception) {
                                            log.error("Failed to send message to $connectionId: ${e.message}")
                                        }
                                    }
                                }
                            } catch (_: CancellationException) {
                                log.debug("WebSocket collection cancelled for $connectionId")
                            } catch (e: Exception) {
                                log.error("Error sending message to WebSocket $connectionId: ${e.message}")
                            }
                        }

                        // Process incoming messages.
                        incoming.consumeEach { frame ->
                            if (frame is Frame.Text) {
                                val receivedText = frame.readText()
                                log.info("Received reload request from $connectionId")
                                log.debug("Broadcasting message to all connections: $receivedText")
                                messageResponseFlow.emit(receivedText)
                            }
                        }
                    } catch (e: Exception) {
                        log.error("WebSocket error for $connectionId: ${e.message}")
                    } finally {
                        activeConnections -= connectionId
                        log.info("WebSocket connection closed: $connectionId")
                        try {
                            close(CloseReason(CloseReason.Codes.NORMAL, "Connection closed"))
                        } catch (e: Exception) {
                            log.debug("Error closing WebSocket connection: ${e.message}")
                        }
                    }
                }
            }
        }.start(wait = wait)
    }
}
