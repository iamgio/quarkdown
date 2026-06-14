package com.quarkdown.cli.preview

import com.quarkdown.cli.exec.ExecutionOutcome
import com.quarkdown.cli.server.WebServerOptions
import com.quarkdown.cli.server.WebServerStarter
import com.quarkdown.core.log.Log
import com.quarkdown.core.pipeline.PipelineOptions
import com.quarkdown.server.ServerEndpoints
import com.quarkdown.server.browser.BrowserLauncher
import com.quarkdown.server.reload.ReloadTrigger
import com.quarkdown.server.stop.Stoppable
import java.util.concurrent.atomic.AtomicBoolean

/**
 * [PreviewStrategy] backed by the bundled Quarkdown web server (the same one exposed by `quarkdown start`).
 *
 * On the first call to [update] the server is started lazily and pointed at the just-generated output directory;
 * subsequent calls reuse the running server and only push a reload trigger over a stateless HTTP call,
 * which the server fans out as Server-Sent Events to every subscribed browser tab.
 * @param port port on which the preview server is started and contacted for reload triggers
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
     * Whether a server startup is currently in flight.
     * Set before [WebServerStarter.start] is invoked and never cleared: once a startup has begun,
     * either it completes (and [server] is assigned) or it fails (and the strategy is unusable anyway).
     * Guards against concurrent [update] callers (e.g. the watcher thread and the main-thread initial compile)
     * both observing `server == null` and binding the same port twice.
     */
    private val starting = AtomicBoolean(false)

    /**
     * Stateless reload trigger pointed at the running server.
     */
    private val reloadTrigger: ReloadTrigger by lazy {
        ReloadTrigger(
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

        if (server != null) {
            // The server is already running from a previous compile in this CLI session;
            // just push a reload trigger.
            reloadTrigger.trigger()
            return
        }

        if (!starting.compareAndSet(false, true)) {
            // A startup is already in flight on another thread; the in-flight onServerReady
            // will reload against the now-updated output directory, so just drop this call.
            return
        }

        val serverOptions =
            WebServerOptions(
                port = this.port,
                targetFile = outcome.directory,
                browserLauncher = browser,
                preferLivePreviewUrl = this.preferLivePreviewUrl,
            )

        Log.info("Starting server...")
        WebServerStarter.start(
            serverOptions,
            onServerStarted = { server = it },
            onServerReady = { reloadTrigger.trigger() },
            wait = true,
        )
    }
}
