package com.quarkdown.server.jsonrpc

/**
 * Handler for one client connection driven by text messages (one JSON-RPC payload per message).
 *
 * This abstraction keeps transports (WebSocket, TCP, ...) decoupled from the wire-format details
 * of the protocol they carry: framing, serialization, and JSON-RPC plumbing live in the handler's
 * own module.
 */
fun interface TextMessageHandler {
    fun handle(
        pollMessage: () -> String?,
        send: (String) -> Unit,
    )
}
