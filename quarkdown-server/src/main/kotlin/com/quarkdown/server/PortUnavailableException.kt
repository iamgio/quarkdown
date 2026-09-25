package com.quarkdown.server

import kotlinx.io.IOException

/**
 * Thrown when the server cannot bind to the requested [port]
 * because it is already in use.
 * @param port the port that could not be bound
 * @param cause the underlying binding failure, if any
 * @see Server.start
 * @see ServerFreePortScanner
 */
class PortUnavailableException(
    val port: Int,
    cause: Throwable? = null,
) : IOException("Port $port is already in use", cause)
