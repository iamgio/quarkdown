package com.quarkdown.cli.server

import com.quarkdown.core.log.Log
import com.quarkdown.server.LocalFileWebServer
import com.quarkdown.server.ServerEndpoints
import com.quarkdown.server.message.ServerMessageSession
import com.quarkdown.server.stop.Stoppable
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
     * @param onServerStarted callback invoked once the server is bound and ready, with the
     *                        handle that can later stop it. Callers should hold on to this
     *                        handle to distinguish "server is running" from "session is open"
     *                        across reload cycles.
     * @param onSessionReady optional callback to invoke when the session is ready
     */
    fun start(
        options: WebServerOptions,
        session: ServerMessageSession,
        onServerStarted: (Stoppable) -> Unit = {},
        onSessionReady: suspend () -> Unit = { },
    ) = runBlocking {
        // Asynchronously start the web server.
        launch(Dispatchers.IO) {
            LocalFileWebServer(options.targetFile).start(
                options.port,
                wait = false,
                onReady = onServerStarted,
            )
            session.init(onSessionReady)
        }

        Log.info("Started web server on port ${options.port}")

        // Optionally the target file in the browser.
        options.browserLauncher?.let {
            try {
                val endpoint = if (options.preferLivePreviewUrl) ServerEndpoints.LIVE_PREVIEW else ServerEndpoints.ROOT
                it.launchLocal(options.port, endpoint)
            } catch (e: Exception) {
                Log.error("Failed to launch URL via ${it::class.simpleName}: ${e.message}")
                Log.debug(e)
            }
        }
    }
}
