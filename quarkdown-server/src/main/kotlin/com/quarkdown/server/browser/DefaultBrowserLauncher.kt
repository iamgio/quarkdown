package com.quarkdown.server.browser

import java.awt.Desktop
import java.net.URI

/**
 * Launcher of a URL in the default browser.
 */
class DefaultBrowserLauncher : BrowserLauncher {
    override fun launch(url: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
            return
        }
        throw UnsupportedOperationException("Cannot open the default browser on this platform.")
    }
}
