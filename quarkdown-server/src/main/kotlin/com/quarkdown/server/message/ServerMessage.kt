package com.quarkdown.server.message

import kotlinx.coroutines.runBlocking

/**
 * A message sent from the client to the server as a WebSocket.
 */
class ServerMessage {
    /**
     * Asynchronously sends the message to a WebSocket server.
     * @param session session to use to send the message
     */
    fun send(session: ServerMessageSession) =
        runBlocking {
            session.send(this@ServerMessage)
        }
}
