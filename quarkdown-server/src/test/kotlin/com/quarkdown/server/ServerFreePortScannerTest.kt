package com.quarkdown.server

import com.quarkdown.server.stop.Stoppable
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Unit tests for [ServerFreePortScanner].
 *
 * Uses a fake [Server] implementation to control which ports succeed or fail,
 * without starting a real server.
 */
class ServerFreePortScannerTest {
    /**
     * Creates a [Server] that throws [IOException] for all ports in [failingPorts]
     * and succeeds (calling [onReady]) for any other port.
     */
    private fun serverFailingOnPorts(failingPorts: Set<Int>): Server =
        object : Server {
            override fun start(
                port: Int,
                wait: Boolean,
                onReady: (Stoppable) -> Unit,
            ) {
                if (port in failingPorts) {
                    throw IOException("Port $port in use")
                }
                onReady(
                    object : Stoppable {
                        override fun stop() {}
                    },
                )
            }
        }

    @Test
    fun `finds first available port`() {
        val failingPorts = setOf(8000, 8001, 8002)
        val scanner = ServerFreePortScanner(serverFailingOnPorts(failingPorts))

        var receivedPort = -1
        scanner.attemptStartUntilPortAvailable(8000) { _, port ->
            receivedPort = port
        }

        assertEquals(8003, receivedPort)
    }

    @Test
    fun `throws when all ports exhausted`() {
        // A server that always fails.
        val alwaysFailing =
            object : Server {
                override fun start(
                    port: Int,
                    wait: Boolean,
                    onReady: (Stoppable) -> Unit,
                ): Unit = throw IOException("Port $port in use")
            }
        val scanner = ServerFreePortScanner(alwaysFailing)

        val exception =
            assertFailsWith<IOException> {
                scanner.attemptStartUntilPortAvailable(65534) { _, _ -> }
            }
        assertEquals("No available port found in range 65534..65535", exception.message)
    }

    @Test
    fun `succeeds on max port`() {
        // Fails on 65534, succeeds on 65535.
        val scanner = ServerFreePortScanner(serverFailingOnPorts(setOf(65534)))

        var receivedPort = -1
        scanner.attemptStartUntilPortAvailable(65534) { _, port ->
            receivedPort = port
        }

        assertEquals(65535, receivedPort)
    }
}
