package com.quarkdown.server

import com.quarkdown.server.stop.Stoppable
import kotlinx.io.IOException

/**
 * Scans for a free port and starts the server on it.
 * @param server server to start
 */
class ServerFreePortScanner(
    private val server: Server,
) {
    /**
     * Attempts to start the server on a free port.
     * @param startingPort port to start from
     * @param onReady callback called when the server is ready to accept requests, with the application and port as arguments
     */
    fun attemptStartUntilPortAvailable(
        startingPort: Int,
        onReady: (Stoppable, port: Int) -> Unit,
    ) {
        var port = startingPort
        while (port <= MAX_PORT) {
            try {
                server.start(port) { stoppable ->
                    onReady(stoppable, port)
                }
                return
            } catch (e: IOException) {
                port++
            }
        }
        throw IOException("No available port found in range $startingPort..$MAX_PORT")
    }

    companion object {
        /**
         * Maximum valid port number.
         */
        private const val MAX_PORT = 65535
    }
}

/**
 * @return a [ServerFreePortScanner] for this server
 */
fun Server.withScanner() = ServerFreePortScanner(this)
