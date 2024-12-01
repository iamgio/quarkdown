package eu.iamgio.quarkdown.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import eu.iamgio.quarkdown.log.Log
import eu.iamgio.quarkdown.server.LocalFileWebServer
import eu.iamgio.quarkdown.server.browser.BrowserLauncher
import eu.iamgio.quarkdown.server.browser.DefaultBrowserLauncher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File

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
    private val targetFile: File by option("-f", "--file", help = "File to serve")
        .file(mustExist = true, canBeDir = true, canBeFile = true)
        .required()

    /**
     * Port to start the server on. If unset, the default port [DEFAULT_PORT] is used.
     */
    private val port: Int by option("-p", "--port", help = "Port to start the server on").int().default(DEFAULT_PORT)

    /**
     * Whether to open the served file in the default browser.
     */
    private val open: Boolean by option("-o", "--open", help = "Open the served file in the default browser").flag()

    override fun run() =
        runBlocking {
            // Asynchronously start the web server.
            launch(Dispatchers.IO) {
                LocalFileWebServer(targetFile).start(port)
            }

            Log.info("Started web server on port $port")

            if (!open) return@runBlocking

            // Open the target file in the default browser.

            val browserLauncher: BrowserLauncher = DefaultBrowserLauncher()
            try {
                browserLauncher.launchLocal(port)
            } catch (e: Exception) {
                Log.error("Failed to launch URL via ${browserLauncher::class.simpleName}: ${e.message}")
                if (Log.isDebug) {
                    e.printStackTrace()
                }
            }
        }
}
