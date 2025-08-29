package com.quarkdown.server.message

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.runBlocking

/**
 * Manages a client WebSocket session to a server.
 * @param host server hostname
 * @param port server port to connect to
 * @param endpoint WebSocket endpoint path segment (without leading slash)
 */
class ServerMessageSession(
    private val host: String = "localhost",
    private val port: Int,
    private val endpoint: String,
) {
    private var session: DefaultClientWebSocketSession? = null

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
     * Whether a WebSocket session has been established and not yet closed.
     */
    val isConnected: Boolean
        get() = session != null

    /**
     * Initializes (if not already initialized) the WebSocket connection.
     * @param onReady callback invoked after the connection is successfully opened
     */
    suspend fun init(onReady: suspend () -> Unit = {}) {
        if (session != null) return

        try {
            session = client.webSocketSession("ws://$host:$port/$endpoint")
            onReady()
            session!!.incoming.consumeEach { }
        } catch (e: Exception) {
            System.err.println("WebSocket closed with exception: ${e.message}")
        } finally {
            close()
        }
    }

    private suspend fun close() {
        session?.close()
        client.close()
        session = null
    }

    /**
     * Sends a [ServerMessage] as a text frame.
     * @throws IllegalStateException if the session is not initialized
     */
    fun send(message: ServerMessage) {
        runBlocking {
            checkNotNull(session).send(Frame.Text(message.toString()))
        }
    }
}
