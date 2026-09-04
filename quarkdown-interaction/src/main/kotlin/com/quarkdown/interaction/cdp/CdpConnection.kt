package com.quarkdown.interaction.cdp

import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.serialization.json.JsonObject

/**
 * A Chrome DevTools Protocol connection to a page target, on top of an open WebSocket session.
 * Purely request-response: protocol events are discarded.
 * @param session the WebSocket session to the target's debugger URL
 */
internal class CdpConnection(
    private val session: DefaultClientWebSocketSession,
) {
    private var nextId: Int = 0

    /**
     * Sends a command and awaits its response.
     * @param method protocol method, e.g. `Page.navigate`
     * @param params optional method parameters
     * @return the command's `result` object
     * @throws CdpException if the browser responds with an error
     */
    suspend fun call(
        method: String,
        params: JsonObject? = null,
    ): JsonObject {
        val id = ++nextId
        session.send(Frame.Text(buildCdpRequest(id, method, params)))

        while (true) {
            val frame = session.incoming.receive() as? Frame.Text ?: continue
            parseCdpResponse(frame.readText(), expectedId = id)?.let { return it }
        }
    }
}
