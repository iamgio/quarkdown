package eu.iamgio.quarkdown.server.message

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A message sent from the client to the server as a WebSocket.
 */
data class ServerMessage(val type: ServerMessageType) {
    /**
     * Asynchronously sends the message to a WebSocket server.
     * @param host host of the server
     * @param port port of the server
     */
    fun send(
        host: String = "localhost",
        port: Int,
    ) = runBlocking {
        launch(Dispatchers.IO) {
            val client =
                HttpClient(CIO) {
                    install(WebSockets)
                }

            client.webSocket("ws://$host:$port/${type.endpoint}") {
                type.send(this)
            }

            client.close()
        }
    }
}
