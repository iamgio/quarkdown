package com.quarkdown.server.message

/**
 * A message sent from the client to the server as a WebSocket text frame.
 */
class ServerMessage {
    /**
     * The text content of this message.
     */
    val content: String = "reload"

    /**
     * Sends the message to a WebSocket server.
     * @param session session to use to send the message
     */
    fun send(session: ServerMessageSession) {
        session.send(this)
    }
}
