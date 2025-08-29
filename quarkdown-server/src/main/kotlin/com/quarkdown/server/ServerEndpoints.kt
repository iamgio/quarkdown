package com.quarkdown.server

/**
 * Endpoints of the local web server.
 * @see LocalFileWebServer
 */
object ServerEndpoints {
    /**
     * Root endpoint, which serves static files.
     */
    const val ROOT = "/"

    /**
     * Endpoint for live preview, which supports live reloading.
     *
     * `/live/file.html` serves the file `file.html` with live reloading support.
     */
    const val LIVE_PREVIEW = "/live"

    /**
     * Endpoint to trigger a reload of the live preview.
     *
     * Sending a message to this endpoint will trigger a reload in all connected clients.
     */
    const val RELOAD_LIVE_PREVIEW = "/reload"
}
