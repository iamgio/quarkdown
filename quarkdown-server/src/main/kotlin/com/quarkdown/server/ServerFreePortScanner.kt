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
     * Only a port reported as occupied before [onReady] starts makes the scan advance:
     * any other failure propagates to the caller instead of being retried on another port.
     * @param startingPort port to start from
     * @param onReady callback called when the server is ready to accept requests, with the application and port as arguments.
     *               It is invoked at most once, no matter how many ports the scan goes through
     * @throws IOException if no port in the `startingPort..65535` range is available
     */
    fun attemptStartUntilPortAvailable(
        startingPort: Int,
        onReady: (Stoppable, port: Int) -> Unit,
    ) {
        var port = startingPort
        while (port <= MAX_PORT) {
            var onReadyStarted = false
            try {
                server.start(port) { stoppable ->
                    onReadyStarted = true
                    onReady(stoppable, port)
                }
                return
            } catch (e: PortUnavailableException) {
                if (onReadyStarted) {
                    throw e
                }
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
