package com.quarkdown.server

import com.quarkdown.server.stop.Stoppable

/**
 * Abstract representation of a server.
 */
interface Server {
    /**
     * Starts the server on [port].
     * @param port port to start the server on
     * @param wait if true, blocks the current thread until the server is stopped
     * @param onReady callback called when the server is ready to accept requests, with the application as argument
     */
    fun start(
        port: Int,
        wait: Boolean = false,
        onReady: (Stoppable) -> Unit = {},
    )
}
