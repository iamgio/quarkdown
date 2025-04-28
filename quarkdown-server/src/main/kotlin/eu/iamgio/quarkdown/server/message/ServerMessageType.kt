package eu.iamgio.quarkdown.server.message

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.Frame

/**
 * A type of message sent a client to the server as a WebSocket.
 * @see ServerMessage
 */
sealed interface ServerMessageType {
    /**
     * URL endpoint that the message should be sent to.
     */
    val endpoint: String

    /**
     * Sends the message to a WebSocket server.
     * @param session WebSocket session
     */
    suspend fun send(session: DefaultClientWebSocketSession)
}

/**
 * Message to reload the browser.
 * When received, the server will communicate with JavaScript to reload the page.
 */
data object Reload : ServerMessageType {
    override val endpoint = "reload"

    override suspend fun send(session: DefaultClientWebSocketSession) {
        session.send(Frame.Text("reload"))
    }
}
