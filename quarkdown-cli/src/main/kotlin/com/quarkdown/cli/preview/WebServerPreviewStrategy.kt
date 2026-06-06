package com.quarkdown.cli.preview

import com.quarkdown.cli.exec.ExecutionOutcome
import com.quarkdown.cli.server.WebServerOptions
import com.quarkdown.cli.server.WebServerStarter
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.server.ServerEndpoints
import com.quarkdown.server.browser.BrowserLauncher
import com.quarkdown.server.message.ServerMessage
import com.quarkdown.server.message.ServerMessageSession
import com.quarkdown.server.stop.Stoppable

/**
 * [PreviewStrategy] backed by the bundled Quarkdown web server (the same one exposed by `quarkdown start`).
 *
 * On the first call to [update] the server is started lazily and pointed at the just-generated output directory;
 * subsequent calls reuse the running server and only push a reload message over the live-preview WebSocket session,
 * which transparently reconnects if the underlying socket has been dropped.
 * @param port port on which the preview server is started and contacted for reload messages
 * @param browser optional browser launcher used to open the served URL the first time the server starts
 * @param preferLivePreviewUrl whether to open the live-preview endpoint URL rather than the direct file URL,
 *                             typically enabled when both preview and watch modes are active
 */
class WebServerPreviewStrategy(
    private val port: Int,
    private val browser: BrowserLauncher?,
    private val preferLivePreviewUrl: Boolean,
) : PreviewStrategy {
    /**
     * Handle to the running preview server, set once on the first compile.
     */
    @Volatile
    private var server: Stoppable? = null

    /**
     * Session to communicate with the server in order to trigger reloads of the preview.
     */
    private val reloadSession: ServerMessageSession by lazy {
        ServerMessageSession(
            port = this.port,
            endpoint = ServerEndpoints.RELOAD_LIVE_PREVIEW,
        )
    }

    override fun update(
        options: PipelineOptions,
        outcome: ExecutionOutcome,
    ) {
        if (outcome.directory == null) {
            Log.error("Cannot update preview: no output directory was generated")
            return
        }

        // Communicates with the server to reload the requested resources.
        // If enabled and the server is not running, also starts the server.
        val serverOptions =
            WebServerOptions(
                port = this.port,
                targetFile = outcome.directory,
                browserLauncher = browser,
                preferLivePreviewUrl = this.preferLivePreviewUrl,
            )

        if (server == null) {
            Log.info("Starting server...")
            WebServerStarter.start(
                serverOptions,
                reloadSession,
                onServerStarted = { server = it },
                onSessionReady = { sendReloadMessage() },
            )
            return
        }

        // The server is already running from a previous compile in this CLI session;
        // just push a reload message. ServerMessageSession reconnects transparently
        // if the underlying WebSocket has dropped.
        sendReloadMessage()
    }

    private fun sendReloadMessage() {
        try {
            ServerMessage().send(reloadSession)
        } catch (e: Exception) {
            Log.error("Could not communicate with the server on port ${this.port}: ${e.message}")
            Log.debug(e)
        }
    }
}
