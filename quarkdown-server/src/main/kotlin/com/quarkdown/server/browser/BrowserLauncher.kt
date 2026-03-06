package com.quarkdown.server.browser

import com.quarkdown.server.SERVER_HOST

/**
 * Launcher of a URL in a specific browser.
 */
interface BrowserLauncher {
    /**
     * Indicates whether the browser is valid and can be launched.
     */
    val isValid: Boolean

    /**
     * Launches a URL in the specified browser.
     * @param url URL to launch
     */
    fun launch(url: String)

    /**
     * Launches a local server URL in the specified browser.
     * @param port port to launch
     * @param endpoint endpoint to launch, defaults to `/`
     */
    fun launchLocal(
        port: Int,
        endpoint: String = "/",
    ) {
        launch("http://$SERVER_HOST:$port$endpoint")
    }
}
