package eu.iamgio.quarkdown.server

import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import kotlin.time.Duration.Companion.seconds

/**
 * Web server that serves a local file.
 * @param targetFile file to serve
 */
class LocalFileWebServer(private val targetFile: File) : Server {
    /**
     * Starts the server on [port].
     * @throws IllegalArgumentException if [targetFile] does not exist
     */
    override fun start(port: Int) {
        if (!targetFile.exists()) throw IllegalArgumentException("Cannot start web server from non-existing file: $targetFile")

        // Connections to the server.
        val sessions = mutableListOf<DefaultWebSocketServerSession>()
        // Mutex to prevent concurrent modification of [sessions].
        val mutex = Mutex()

        // Sends a frame to all connected clients.
        suspend fun sendBroadcast(frame: Frame) {
            mutex.withLock {
                sessions.forEach { it.send(frame) }
            }
        }

        embeddedServer(Netty, port) {
            install(WebSockets) {
                pingPeriod = 5.seconds
                timeout = 10.seconds
                maxFrameSize = Long.MAX_VALUE
            }
            routing {
                webSocket("/reload") {
                    // Reload flow:
                    // 1. The Quarkdown CLI sends a WebSocket to the server after the new files are generated.
                    // 2. The server forwards the message to all connected clients, e.g. the browser (see websockets.js).

                    log.info("Reload requested")
                    mutex.withLock { sessions += this }

                    for (frame in incoming) {
                        sendBroadcast(frame)
                    }
                }

                // Serve the target file.
                staticFiles("/", targetFile)
            }
        }.start(wait = true)
    }
}
