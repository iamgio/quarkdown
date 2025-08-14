package com.quarkdown.server.browser

import java.awt.Desktop
import java.net.URI

/**
 * Launcher of a URL in the default browser.
 */
class DefaultBrowserLauncher : BrowserLauncher {
    override val isValid: Boolean
        get() = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)

    override fun launch(url: String) {
        Desktop.getDesktop().browse(URI(url))
    }
}
