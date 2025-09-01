package com.quarkdown.server

import com.quarkdown.server.endpoints.LivePreviewEndpoint
import com.quarkdown.server.endpoints.ReloadEndpoint
import com.quarkdown.server.stop.KtorStoppableAdapter
import com.quarkdown.server.stop.Stoppable
import io.ktor.server.application.ServerReady
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import java.io.File
import kotlin.time.Duration.Companion.seconds

/**
 * Web server that:
 * - Serves local file at [targetFile];
 * - Supports live preview of HTML files at `/live/{file...}` ([LivePreviewEndpoint]);
 * - Supports live-reloading via WebSockets at `/reload` ([ReloadEndpoint]).
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
     */
    override fun start(
        port: Int,
        wait: Boolean,
        onReady: (Stoppable) -> Unit,
    ) {
        if (!targetFile.exists()) {
            throw IllegalArgumentException("Cannot start web server for non-existing file: $targetFile")
        }

        embeddedServer(Netty, port) {
            install(WebSockets) {
                pingPeriod = 10.seconds
                timeout = 15.seconds
                maxFrameSize = Long.MAX_VALUE
            }

            monitor.subscribe(ServerReady) { onReady(KtorStoppableAdapter(this)) }

            routing {
                // Serves the target file directly at the root path.
                staticFiles(ServerEndpoints.ROOT, targetFile)

                // Serves files for live preview.
                get(ServerEndpoints.LIVE_PREVIEW + "/{file...}") {
                    livePreview.handleRequest(call, port)
                }

                // WebSocket endpoint for reloading live previews.
                webSocket(ServerEndpoints.RELOAD_LIVE_PREVIEW) {
                    reload.handleRequest(this)
                }
            }
        }.start(wait = wait)
    }
}
