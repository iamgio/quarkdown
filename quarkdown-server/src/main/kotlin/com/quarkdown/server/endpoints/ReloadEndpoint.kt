package com.quarkdown.server.endpoints

import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Handler of the reload endpoint (`/reload`) which manages WebSocket connections for live reloading.
 *
 * Whenever a client sends a message, it is broadcast to all connected clients.
 *
 * A distinction between sender and receiver is not made.
 * In practice, the sender is Quarkdown CLI and the receivers are the browser clients
 * that were served by the live preview endpoint (`/live/<file>`).
 */
class ReloadEndpoint {
    private val logger: Logger = LoggerFactory.getLogger(ReloadEndpoint::class.java)

    // Trackers of active connections.
    private val activeConnections = ConcurrentHashMap<String, Boolean>()
    private val connectionCounter = AtomicInteger(0)

    // Shared flow to broadcast messages to all connected clients.
    // No replay is needed: a newly connected client already has the latest content,
    // and replaying stale messages would trigger redundant reloads.
    private val messageResponseFlow = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 10)
    private val sharedFlow = messageResponseFlow.asSharedFlow()

    /**
     * Handles the logic for the WebSocket endpoint.
     * - A new tracked connection is created;
     * - Incoming messages from a client are broadcast to all connected clients.
     * @param session the WebSocket session
     */
    suspend fun handleRequest(session: WebSocketSession) {
        val connectionId = newConnectionId()
        registerConnection(connectionId)
        val forwarderJob = session.launch { forwardMessagesToClient(session, connectionId) }

        try {
            receiveAndBroadcast(session, connectionId)
        } catch (e: CancellationException) {
            logger.debug("WebSocket cancelled: $connectionId")
            throw e
        } catch (e: Exception) {
            logger.error("WebSocket error for $connectionId: ${e.message}")
        } finally {
            cleanupConnection(session, connectionId, forwarderJob)
        }
    }

    /**
     * @return a new unique connection ID
     */
    private fun newConnectionId(): String = "connection-${connectionCounter.incrementAndGet()}"

    /**
     * Registers a new active connection.
     */
    private fun registerConnection(connectionId: String) {
        activeConnections[connectionId] = true
        logger.info("WebSocket connection established: $connectionId")
    }

    /**
     * Forwards messages from the shared flow to the specific client session, if still active.
     */
    private suspend fun forwardMessagesToClient(
        session: WebSocketSession,
        connectionId: String,
    ) {
        try {
            sharedFlow.collect { message ->
                if (activeConnections.containsKey(connectionId)) {
                    session.send(Frame.Text(message))
                    logger.debug("Sent message to $connectionId: $message")
                }
            }
        } catch (_: CancellationException) {
            logger.debug("Forwarder cancelled for $connectionId")
        } catch (e: Exception) {
            logger.error("Error sending message to $connectionId: ${e.message}")
        }
    }

    /**
     * Listens for messages from the client session and broadcasts them to all connected clients.
     */
    private suspend fun receiveAndBroadcast(
        session: WebSocketSession,
        connectionId: String,
    ) {
        session.incoming.consumeEach { frame ->
            if (frame is Frame.Text) {
                val receivedText = frame.readText()
                logger.info("Received reload request from $connectionId")
                logger.debug("Broadcasting message to all connections: $receivedText")
                messageResponseFlow.emit(receivedText)
            }
        }
    }

    /**
     * Cleans up resources associated with a connection.
     */
    private suspend fun cleanupConnection(
        session: WebSocketSession,
        connectionId: String,
        forwarderJob: Job,
    ) {
        activeConnections.remove(connectionId)
        logger.info("WebSocket connection closed: $connectionId")
        try {
            forwarderJob.cancel()
            session.close(CloseReason(CloseReason.Codes.NORMAL, "Connection closed"))
        } catch (e: Exception) {
            logger.debug("Error during cleanup for $connectionId: ${e.message}")
        }
    }
}
