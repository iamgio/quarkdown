package eu.iamgio.quarkdown.server

import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import java.io.File

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
            routing {
                staticFiles("/", targetFile)
            }
        }.start(wait = true)
    }
}
