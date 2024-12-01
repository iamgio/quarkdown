package eu.iamgio.quarkdown.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.server.LocalFileWebServer

/**
 * The default port to start the web server on.
 */
private const val DEFAULT_PORT = 8089

/**
 * Command to start a web server serving a local file.
 * @see LocalFileWebServer
 */
class StartWebServerCommand : CliktCommand(name = "start") {
    /**
     * File to serve.
     */
    private val targetFile by option("-f", "--file", help = "File to serve")
        .file(mustExist = true, canBeDir = true, canBeFile = true)
        .required()

    /**
     * Port to start the server on. If unset, the default port [DEFAULT_PORT] is used.
     */
    private val port: Int? by option("-p", "--port", help = "Port to start the server on").int()

    override fun run() {
        LocalFileWebServer(targetFile).start(port ?: DEFAULT_PORT)
        Log.info("Started web server on port ${port ?: DEFAULT_PORT}")
    }
}
