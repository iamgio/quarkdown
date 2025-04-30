package com.quarkdown.cli.server

import com.quarkdown.core.log.Log
import com.quarkdown.server.LocalFileWebServer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Starter of the web server.
 */
object WebServerStarter {
    /**
     * Starts the web server.
     * @param options options to start the server with
     */
    fun start(options: WebServerOptions) =
        runBlocking {
            // Asynchronously start the web server.
            launch(Dispatchers.IO) {
                LocalFileWebServer(options.targetFile).start(options.port)
            }

            Log.info("Started web server on port ${options.port}")

            // Open the target file in the default browser.

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
