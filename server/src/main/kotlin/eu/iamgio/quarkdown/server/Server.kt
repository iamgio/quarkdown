package eu.iamgio.quarkdown.server

/**
 * Abstract representation of a server.
 */
interface Server {
    /**
     * Starts the server on [port].
     */
    fun start(
        port: Int,
        onReady: (Server) -> Unit = {},
    )
}
