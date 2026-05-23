package com.quarkdown.server.message

import com.quarkdown.core.log.Log
import com.quarkdown.server.SERVER_HOST
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.isActive
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Manages a client WebSocket session to a server.
 *
 * The session lifecycle is decoupled from the server's: if the underlying WebSocket
 * drops, [send] transparently reconnects against the still-running server.
 * Callers that need to stop the session explicitly should call [close].
 *
 * @param host server hostname
 * @param port server port to connect to
 * @param endpoint WebSocket endpoint path segment (without leading slash)
 */
class ServerMessageSession(
    private val host: String = SERVER_HOST,
    private val port: Int,
    private val endpoint: String,
) {
    @Volatile
    private var session: DefaultClientWebSocketSession? = null

    /**
     * Serializes session creation to avoid leaking parallel sockets when [send] races with [init].
     */
    private val sessionLock = Mutex()

    private val client: HttpClient by lazy {
        HttpClient(CIO) {
            engine {
                endpoint {
                    connectTimeout = 10_000
                    requestTimeout = 10_000
                    connectAttempts = 5
                }
            }
            install(WebSockets)
        }
    }

    /**
     * Returns the current session if active, otherwise opens a new one.
     * Session creation is serialized through [sessionLock]; any inactive WebSocket is closed
     * (best-effort) before being replaced so concurrent callers can't leak parallel sockets.
     * The shared [client] is intentionally left alive so future reconnects still work.
     */
    private suspend fun ensureSession(): DefaultClientWebSocketSession {
        session?.takeIf { it.isActive }?.let { return it }
        return sessionLock.withLock {
            session?.takeIf { it.isActive }?.let { return@withLock it }
            session?.let { stale -> runCatching { stale.close() } }
            client.webSocketSession("ws://$host:$port/$endpoint").also { session = it }
        }
    }

    /**
     * Opens the WebSocket session (if not already open) and invokes [onReady] once the
     * connection is established. Then blocks until the session is closed, so callers
     * that rely on this method to keep their thread alive (e.g. the standalone server
     * command) continue to work.
     *
     * The session field is intentionally not cleared on exit: the server is on its own
     * lifecycle and may still be running, so a later [send] should be able to reconnect
     * rather than fail or trigger a server restart.
     */
    suspend fun init(onReady: suspend () -> Unit = {}) {
        try {
            ensureSession()
            onReady()
            session?.incoming?.consumeEach { }
        } catch (e: Exception) {
            Log.error("WebSocket session failed: ${e.message}")
            Log.debug(e)
        }
    }

    /**
     * Releases the session and the underlying HTTP client. After this call, no further
     * [send]s will succeed without a new session being created externally.
     */
    suspend fun close() {
        session?.close()
        client.close()
        session = null
    }

    /**
     * Sends a [ServerMessage] as a text frame, opening (or reopening) the session if needed.
     */
    fun send(message: ServerMessage) {
        runBlocking {
            ensureSession().send(Frame.Text(message.content))
        }
    }
}
