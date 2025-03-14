package eu.iamgio.quarkdown.server

import eu.iamgio.quarkdown.server.stop.Stoppable

/**
 * Abstract representation of a server.
 */
interface Server {
    /**
     * Starts the server on [port].
     * @param onReady callback called when the server is ready to accept requests, with the application as argument
     */
    fun start(
        port: Int,
        onReady: (Stoppable) -> Unit = {},
    )
}
