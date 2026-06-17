package com.quarkdown.server.endpoints

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.sse.ServerSSESession
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Default reload event payload broadcast to subscribers.
 */
private const val DEFAULT_RELOAD_PAYLOAD = "reload"

/**
 * Handler of the reload endpoint (`/reload`).
 *
 * The endpoint is split across two HTTP methods, both routed on the same path:
 * - `GET /reload`: opens a Server-Sent Events stream that receives `reload` events whenever a trigger occurs.
 *   In practice, browser clients served by the live preview endpoint (`/live/<file>`) subscribe here
 *   and use the incoming events to refresh the embedded page.
 * - `POST /reload`: broadcasts a `reload` event to every active subscriber. The optional request body
 *   (text/plain) is forwarded as the event payload; if absent, a default value is used.
 *   In practice, Quarkdown CLI calls this endpoint after each successful compilation.
 */
class ReloadEndpoint {
    private val logger: Logger = LoggerFactory.getLogger(ReloadEndpoint::class.java)

    private val subscriberCounter = AtomicInteger(0)

    /**
     * Shared flow that broadcasts reload events to all currently subscribed sessions.
     * No replay is needed: a newly connected client already has the latest content,
     * and replaying stale events would trigger redundant reloads.
     */
    private val broadcasts = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 10)
    private val sharedBroadcasts = broadcasts.asSharedFlow()

    /**
     * Handles a new SSE subscription: keeps the session open and forwards every broadcast event to it.
     * Returns when the client disconnects (the surrounding [ServerSSESession] is then closed by Ktor).
     */
    suspend fun handleSubscription(session: ServerSSESession) {
        val subscriberId = "subscriber-${subscriberCounter.incrementAndGet()}"
        logger.info("SSE subscriber connected: $subscriberId")
        try {
            sharedBroadcasts.collect { payload ->
                session.send(ServerSentEvent(data = payload))
                logger.debug("Sent event to $subscriberId: $payload")
            }
        } catch (e: CancellationException) {
            logger.debug("SSE subscription cancelled: $subscriberId")
            throw e
        } catch (e: IOException) {
            logger.debug("SSE subscriber $subscriberId disconnected: ${e.message ?: e.javaClass.simpleName}")
        } catch (e: Exception) {
            logger.error("SSE subscription error for $subscriberId: ${e.message ?: e.javaClass.simpleName}", e)
        } finally {
            logger.info("SSE subscriber disconnected: $subscriberId")
        }
    }

    /**
     * Handles a reload trigger: broadcasts a reload event to every subscriber
     * and responds with `204 No Content`.
     */
    suspend fun handleTrigger(call: ApplicationCall) {
        logger.info("Reload triggered, broadcasting to subscribers")
        broadcasts.emit(DEFAULT_RELOAD_PAYLOAD)
        call.respond(HttpStatusCode.NoContent)
    }
}
