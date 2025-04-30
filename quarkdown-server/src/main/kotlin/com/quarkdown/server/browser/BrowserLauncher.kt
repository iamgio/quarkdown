package com.quarkdown.server.browser

/**
 * Launcher of a URL in a specific browser.
 */
interface BrowserLauncher {
    /**
     * Launches a URL in the specified browser.
     * @param url URL to launch
     */
    fun launch(url: String)

    /**
     * Launches a `localhost:<port>` in the specified browser.
     * @param port port to launch
     */
    fun launchLocal(port: Int) {
        launch("http://localhost:$port")
    }
}
