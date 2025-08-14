package com.quarkdown.server.browser

/**
 * A fake browser launcher that does not perform any action.
 * This is needed to allow `--browser none` option in the CLI.
 */
class NoneBrowserLauncher : BrowserLauncher {
    override val isValid: Boolean
        get() = true

    override fun launch(url: String) {}
}
