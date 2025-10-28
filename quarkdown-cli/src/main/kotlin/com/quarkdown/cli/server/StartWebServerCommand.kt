package com.quarkdown.cli.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import com.quarkdown.server.LocalFileWebServer
import com.quarkdown.server.ServerEndpoints
import com.quarkdown.server.browser.BrowserLauncher
import com.quarkdown.server.message.ServerMessageSession
import java.io.File

/**
 * The default port to start the web server on.
 */
const val DEFAULT_SERVER_PORT = 8089

/**
 * Command to start a web server serving a local file,
 * allowing for live reloading.
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
     * Port to start the server on. If unset, the default port [DEFAULT_SERVER_PORT] is used.
     */
    private val port: Int by option("-p", "--port", help = "Port to start the server on")
        .int()
        .default(DEFAULT_SERVER_PORT)

    /**
     * Optional browser to open the served file in.
     */
    private val browser: BrowserLauncher? by browserLauncherOption()

    override fun run() {
        val options = WebServerOptions(port, targetFile, browser, preferLivePreviewUrl = true)
        val session =
            ServerMessageSession(
                port = port,
                endpoint = ServerEndpoints.RELOAD_LIVE_PREVIEW,
            )

        WebServerStarter.start(options, session)
    }
}
