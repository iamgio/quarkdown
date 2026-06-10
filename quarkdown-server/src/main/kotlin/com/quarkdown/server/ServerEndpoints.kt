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
     * Endpoint that drives live-preview reloading.
     *
     * - `GET` opens a Server-Sent Events stream that delivers reload events to subscribers.
     * - `POST` broadcasts a reload event to every active subscriber.
     */
    const val RELOAD_LIVE_PREVIEW = "/reload"
}
