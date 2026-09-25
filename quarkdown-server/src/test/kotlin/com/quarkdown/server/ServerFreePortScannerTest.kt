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
 * @see ServerPortFallbackTest for the same behavior against a real server
 */
class ServerFreePortScannerTest {
    /**
     * Creates a [Server] that reports all ports in [failingPorts] as occupied
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
                    throw PortUnavailableException(port)
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
                ): Unit = throw PortUnavailableException(port)
            }
        val scanner = ServerFreePortScanner(alwaysFailing)

        val exception =
            assertFailsWith<IOException> {
                scanner.attemptStartUntilPortAvailable(65534) { _, _ -> }
            }
        assertEquals("No available port found in range 65534..65535", exception.message)
    }

    @Test
    fun `a failing onReady is not retried on another port`() {
        val scanner = ServerFreePortScanner(serverFailingOnPorts(emptySet()))
        var attempts = 0

        assertFailsWith<IOException> {
            scanner.attemptStartUntilPortAvailable(8000) { _, _ ->
                attempts++
                throw IOException("Something went wrong once the server was up")
            }
        }

        assertEquals(1, attempts)
    }

    @Test
    fun `a port reported as occupied after onReady started is not retried`() {
        // A server that binds successfully, then fails once the callback has run.
        val failingAfterReady =
            object : Server {
                override fun start(
                    port: Int,
                    wait: Boolean,
                    onReady: (Stoppable) -> Unit,
                ) {
                    onReady(
                        object : Stoppable {
                            override fun stop() {}
                        },
                    )
                    throw PortUnavailableException(port)
                }
            }
        val scanner = ServerFreePortScanner(failingAfterReady)
        var attempts = 0

        val exception =
            assertFailsWith<PortUnavailableException> {
                scanner.attemptStartUntilPortAvailable(8000) { _, _ -> attempts++ }
            }

        assertEquals(8000, exception.port)
        assertEquals(1, attempts)
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
