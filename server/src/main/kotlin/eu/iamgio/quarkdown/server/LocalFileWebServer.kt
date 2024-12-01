package eu.iamgio.quarkdown.server

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
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

        embeddedServer(Netty, port) {
            install(WebSockets) {
                pingPeriod = 5.seconds
                timeout = 10.seconds
                maxFrameSize = Long.MAX_VALUE
            }
            routing {
                webSocket("/reload") {
                    println("Reload requested")
                }

                staticFiles("/", targetFile)
            }
        }.start(wait = true)
    }
}
