package com.quarkdown.server

import java.io.File
import java.net.ServerSocket
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests the interaction between a real [LocalFileWebServer] and [ServerFreePortScanner]
 * when the requested port is already taken by another process.
 */
class ServerPortFallbackTest {
    private val directory: File =
        Files
            .createTempDirectory("quarkdown-port-fallback")
            .toFile()
            .apply { resolve("index.html").writeText("<html><body>Quarkdown</body></html>") }

    @Test
    fun `starting on an occupied port reports the port as unavailable`() {
        ServerSocket(0).use { occupied ->
            val exception =
                assertFailsWith<PortUnavailableException> {
                    LocalFileWebServer(directory).start(occupied.localPort)
                }
            assertEquals(occupied.localPort, exception.port)
        }
    }

    @Test
    fun `scanner falls through to the next free port`() {
        ServerSocket(0).use { occupied ->
            var readyPort = -1
            LocalFileWebServer(directory)
                .withScanner()
                .attemptStartUntilPortAvailable(occupied.localPort) { server, port ->
                    readyPort = port
                    server.stop()
                }
            assertTrue(
                readyPort > occupied.localPort,
                "The server should have started on a port past the occupied one, but got $readyPort",
            )
        }
    }
}
