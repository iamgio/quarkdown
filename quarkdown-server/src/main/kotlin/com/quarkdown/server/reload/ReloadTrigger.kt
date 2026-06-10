package com.quarkdown.server.reload

import com.quarkdown.core.log.Log
import com.quarkdown.server.SERVER_HOST
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.request.post
import kotlinx.coroutines.runBlocking

/**
 * Stateless trigger for live-preview reloads.
 *
 * Each call performs a fire-and-forget `POST` to the server's reload endpoint;
 * the server then broadcasts the event to every connected Server-Sent Events subscriber.
 * Unlike a persistent session, there is no connection to keep alive: a failed call simply
 * means no clients will reload, and the next attempt is independent.
 *
 * @param host server hostname
 * @param port server port to contact
 * @param endpoint reload endpoint path (with leading slash)
 */
class ReloadTrigger(
    private val host: String = SERVER_HOST,
    private val port: Int,
    private val endpoint: String,
) {
    private val client: HttpClient by lazy {
        HttpClient(CIO) {
            engine {
                endpoint {
                    connectTimeout = 10_000
                    requestTimeout = 10_000
                    connectAttempts = 5
                }
            }
        }
    }

    /**
     * Sends a reload trigger to the server. Errors are logged but not rethrown,
     * since a missed reload is recoverable: the next compile will issue another.
     */
    fun trigger() {
        runBlocking {
            try {
                client.post("http://$host:$port$endpoint")
            } catch (e: Exception) {
                Log.error("Could not contact reload endpoint at $host:$port: ${e.message}")
                Log.debug(e)
            }
        }
    }

    /**
     * Releases the underlying HTTP client. After this call no further triggers can be sent.
     */
    fun close() {
        client.close()
    }
}
