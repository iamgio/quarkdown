package com.quarkdown.server

import com.quarkdown.core.log.Log
import com.quarkdown.interaction.os.OsUtils
import com.quarkdown.server.endpoints.LivePreviewEndpoint
import com.quarkdown.server.endpoints.ReloadEndpoint
import com.quarkdown.server.stop.KtorStoppableAdapter
import com.quarkdown.server.stop.Stoppable
import io.ktor.server.application.ServerReady
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import io.ktor.server.sse.sse
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.net.BindException

/**
 * Loopback address used by the server and all clients.
 * On Windows, `localhost` may resolve to `::1` (IPv6) and cause connection timeouts,
 * so the explicit IPv4 loopback (`127.0.0.1`) is used instead.
 */
val SERVER_HOST: String =
    OsUtils.dependent(
        windows = { "127.0.0.1" },
        unix = { "localhost" },
    )

/**
 * Web server that:
 * - Serves local file at [targetFile];
 * - Supports live preview of HTML files at `/live/{file...}` ([LivePreviewEndpoint]);
 * - Supports live reloading at `/reload` ([ReloadEndpoint]):
 *   `GET` opens a Server-Sent Events stream for subscribers (browser clients),
 *   `POST` broadcasts a reload event to every active subscriber (Quarkdown CLI).
 * @param targetFile file to serve
 */
class LocalFileWebServer(
    private val targetFile: File,
) : Server {
    private val livePreview = LivePreviewEndpoint(targetFile)
    private val reload = ReloadEndpoint()

    /**
     * Starts the server on [port].
     * @throws IllegalArgumentException if [targetFile] does not exist
     * @throws PortUnavailableException if [port] is already in use
     */
    override fun start(
        port: Int,
        wait: Boolean,
        onReady: (Stoppable) -> Unit,
    ) {
        if (!targetFile.exists()) {
            throw IllegalArgumentException("Cannot start web server for non-existing file: $targetFile")
        }

        var ready = false
        val server =
            CoroutineScope(engineExceptionHandler { ready }).embeddedServer(CIO, port) {
                install(SSE)

                routing {
                    // Serves the target file directly at the root path.
                    staticFiles(ServerEndpoints.ROOT, targetFile)

                    // Serves files for live preview.
                    get(ServerEndpoints.LIVE_PREVIEW + "/{file...}") {
                        livePreview.handleRequest(call)
                    }

                    // SSE stream that delivers reload events to subscribers.
                    sse(ServerEndpoints.RELOAD_LIVE_PREVIEW) {
                        reload.handleSubscription(this)
                    }

                    // Trigger: broadcasts a reload event to every active SSE subscriber.
                    post(ServerEndpoints.RELOAD_LIVE_PREVIEW) {
                        reload.handleTrigger(call)
                    }
                }
            }

        server.monitor.subscribe(ServerReady) {
            ready = true
            onReady(KtorStoppableAdapter(server.application))
        }

        try {
            server.start(wait = wait)
        } catch (e: Exception) {
            if (e.isPortUnavailable(ready)) {
                throw PortUnavailableException(port, e)
            }
            throw e
        }
    }
}

/**
 * Creates the handler of the exceptions raised by the engine's own coroutines.
 * @param isReady whether the server has become ready to accept requests
 */
private fun engineExceptionHandler(isReady: () -> Boolean) =
    CoroutineExceptionHandler { _, throwable ->
        when {
            throwable.isPortUnavailable(isReady()) -> Log.debug(throwable)
            else -> Log.error("Unhandled server error", throwable)
        }
    }

/**
 * @param ready whether the server had become ready to accept requests when this throwable was raised
 * @return whether this throwable is a failure to bind the port the server was starting on
 */
private fun Throwable.isPortUnavailable(ready: Boolean): Boolean = !ready && isCausedByBindFailure()

/**
 * @return whether this throwable, or any throwable in its cause chain, is a port binding failure
 */
private fun Throwable.isCausedByBindFailure(): Boolean =
    generateSequence(this) { throwable -> throwable.cause?.takeUnless { it === throwable } }
        .any { it is BindException }
