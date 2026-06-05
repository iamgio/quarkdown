package com.quarkdown.server.jsonrpc

import com.quarkdown.server.Server
import com.quarkdown.server.stop.KtorStoppableAdapter
import com.quarkdown.server.stop.Stoppable
import io.ktor.server.application.ServerReady
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.LinkedBlockingQueue
import kotlin.time.Duration.Companion.seconds

private val CLOSE_SIGNAL = Any()

/**
 * Upper bound on a single WebSocket frame, in bytes. Setting this to [Long.MAX_VALUE] would let
 * a misbehaving client exhaust heap memory by sending one huge frame, so we cap at 16 MiB —
 * comfortably above any realistic single LSP payload (large file `didOpen`, fat `semanticTokens`
 * responses) while still bounding the blast radius.
 */
private const val MAX_WS_FRAME_SIZE: Long = 16L * 1024 * 1024

/**
 * WebSocket server that exposes a text-message protocol (typically JSON-RPC, e.g. LSP) over WebSockets.
 *
 * Each WebSocket connection spins up a fresh [TextMessageHandler] invocation on a worker thread.
 * Incoming WebSocket text frames are queued for the handler to poll; outbound messages are pushed
 * straight onto the WebSocket. The transport stays protocol-agnostic: framing and serialization
 * are entirely the handler's concern (e.g. LSP4J's JSON-RPC plumbing lives in `quarkdown-lsp`).
 *
 * @param path WebSocket endpoint path (e.g. `/lsp`)
 * @param handler invoked once per connection
 */
class JsonRpcWebSocketServer(
    private val path: String,
    private val handler: TextMessageHandler,
) : Server {
    override fun start(
        port: Int,
        wait: Boolean,
        onReady: (Stoppable) -> Unit,
    ) {
        val server =
            embeddedServer(Netty, port) {
                install(WebSockets) {
                    pingPeriod = 10.seconds
                    timeout = 30.seconds
                    maxFrameSize = MAX_WS_FRAME_SIZE
                }
                routing {
                    webSocket(path) {
                        bridgeConnection(handler)
                    }
                }
            }

        server.monitor.subscribe(ServerReady) { onReady(KtorStoppableAdapter(server.application)) }
        server.start(wait = wait)
    }
}

/**
 * Bridges this WebSocket session to a [TextMessageHandler].
 *
 * Two channels carry messages:
 * - An unbounded outbound [Channel] drained by a coroutine that forwards to the WS.
 * - An unbounded inbound [LinkedBlockingQueue] (with [CLOSE_SIGNAL] sentinel) drained by the
 *   handler's `pollMessage` callback, which runs on the LSP4J listener thread (not a coroutine).
 *
 * Cleanup signals the handler to stop by enqueuing the sentinel, then waits for the handler
 * coroutine to finish so resources are released before the WebSocket closes.
 */
private suspend fun DefaultWebSocketServerSession.bridgeConnection(handler: TextMessageHandler) {
    val inbound = LinkedBlockingQueue<Any>()
    val outbound = Channel<String>(Channel.UNLIMITED)

    try {
        coroutineScope {
            val outPump =
                launch {
                    for (msg in outbound) outgoing.send(Frame.Text(msg))
                }

            val handlerJob =
                launch(Dispatchers.IO) {
                    handler.handle(
                        pollMessage = {
                            when (val item = inbound.take()) {
                                is String -> item
                                else -> null
                            }
                        },
                        send = { msg -> outbound.trySend(msg) },
                    )
                }

            try {
                for (frame in incoming) {
                    if (frame is Frame.Text) inbound.offer(frame.readText())
                }
            } finally {
                withContext(NonCancellable) {
                    inbound.offer(CLOSE_SIGNAL)
                    handlerJob.join()
                    outbound.close()
                    outPump.join()
                }
            }
        }
    } finally {
        runCatching { close(CloseReason(CloseReason.Codes.NORMAL, "session ended")) }
    }
}
