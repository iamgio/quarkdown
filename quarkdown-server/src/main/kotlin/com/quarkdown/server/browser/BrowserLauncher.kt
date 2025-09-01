package com.quarkdown.server.browser

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
     * Launches a `localhost:<port>` in the specified browser.
     * @param port port to launch
     * @param endpoint endpoint to launch, defaults to `/`
     */
    fun launchLocal(
        port: Int,
        endpoint: String = "/",
    ) {
        launch("http://localhost:$port$endpoint")
    }
}
