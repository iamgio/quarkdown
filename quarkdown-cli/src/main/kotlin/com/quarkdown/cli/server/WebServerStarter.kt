package com.quarkdown.cli.server

import com.quarkdown.core.log.Log
import com.quarkdown.server.LocalFileWebServer
import com.quarkdown.server.ServerEndpoints
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
     * @param onServerStarted callback invoked once the server is bound and ready, with the
     *                        handle that can later stop it. Callers should hold on to this
     *                        handle to distinguish "server is running" from later reload cycles.
     * @param onServerReady callback invoked once the server is ready to accept reload triggers.
     *                      Typically used to push an initial reload so any browser tab that was
     *                      already open (waiting for the SSE endpoint) refreshes immediately.
     * @param wait if true, blocks the caller until the server stops. Useful for standalone server
     *             invocations (`quarkdown start`) where there is nothing else keeping the process alive.
     */
    fun start(
        options: WebServerOptions,
        onServerStarted: (Stoppable) -> Unit = {},
        onServerReady: suspend () -> Unit = {},
        wait: Boolean = false,
    ) = runBlocking {
        val scope = this
        launch(Dispatchers.IO) {
            LocalFileWebServer(options.targetFile).start(
                options.port,
                wait = wait,
                onReady = { handle ->
                    onServerStarted(handle)
                    scope.launch(Dispatchers.IO) { onServerReady() }
                },
            )
        }

        Log.info("Started web server on port ${options.port}")

        // Optionally open the target file in the browser.
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
