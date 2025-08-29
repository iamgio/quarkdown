package com.quarkdown.cli.server

import com.quarkdown.core.log.Log
import com.quarkdown.server.LocalFileWebServer
import com.quarkdown.server.message.ServerMessageSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Starter of the web server.
 */
object WebServerStarter {
    /**
     * Starts the web server which serves the specified file and allows for live reloading.
     * @param options options to start the server with
     * @param session session to use to communicate with the web server
     * @param onSessionReady optional callback to invoke when the session is ready
     */
    fun start(
        options: WebServerOptions,
        session: ServerMessageSession,
        onSessionReady: suspend () -> Unit = { },
    ) = runBlocking {
        // Asynchronously start the web server.
        launch(Dispatchers.IO) {
            LocalFileWebServer(options.targetFile).start(options.port, wait = false)
            session.init(onSessionReady)
        }

        Log.info("Started web server on port ${options.port}")

        // Optionally the target file in the browser.
        options.browserLauncher?.let {
            try {
                it.launchLocal(options.port)
            } catch (e: Exception) {
                Log.error("Failed to launch URL via ${it::class.simpleName}: ${e.message}")
                Log.debug(e)
            }
        }
    }
}
