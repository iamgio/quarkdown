package eu.iamgio.quarkdown.server

import eu.iamgio.quarkdown.server.stop.KtorStoppableAdapter
import eu.iamgio.quarkdown.server.stop.Stoppable
import io.ktor.server.application.ServerReady
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.time.Duration.Companion.seconds

/**
 * Web server that serves a local file.
 * @param targetFile file to serve
 */
class LocalFileWebServer(
    private val targetFile: File,
) : Server {
    /**
     * Starts the server on [port].
     * @throws IllegalArgumentException if [targetFile] does not exist
     */
    override fun start(
        port: Int,
        onReady: (Stoppable) -> Unit,
    ) {
        if (!targetFile.exists()) throw IllegalArgumentException("Cannot start web server from non-existing file: $targetFile")

        // Shared flow to broadcast messages to all connected clients.
        val messageResponseFlow = MutableSharedFlow<String>()
        val sharedFlow = messageResponseFlow.asSharedFlow()

        embeddedServer(Netty, port) {
            install(WebSockets) {
                pingPeriod = 5.seconds
                timeout = 10.seconds
                maxFrameSize = Long.MAX_VALUE
            }

            monitor.subscribe(ServerReady) { onReady(KtorStoppableAdapter(this)) }

            routing {
                webSocket("/reload") {
                    // Reload flow:
                    // 1. The Quarkdown CLI sends a WebSocket to the server after the new files are generated.
                    // 2. The server forwards the message to all connected clients, e.g. the browser (see websockets.js).

                    log.info("Reload requested")

                    // Forward messages to all connected clients as a broadcast.

                    val job =
                        launch {
                            sharedFlow.collect { message ->
                                send(Frame.Text(message))
                            }
                        }

                    runCatching {
                        incoming.consumeEach { frame ->
                            if (frame is Frame.Text) {
                                val receivedText = frame.readText()
                                messageResponseFlow.emit(receivedText)
                            }
                        }
                    }.onFailure { exception ->
                        log.error("WebSocket error: ${exception.message}")
                    }.also {
                        job.cancel()
                    }
                }

                // Serve the target file.
                staticFiles("/", targetFile)
            }
        }.start(wait = true)
    }
}
