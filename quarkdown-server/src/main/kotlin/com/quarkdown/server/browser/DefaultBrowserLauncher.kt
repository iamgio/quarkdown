package com.quarkdown.server.browser

import java.awt.Desktop
import java.net.URI

/**
 * Launcher of a URL in the default system browser.
 *
 * On platforms where the Java AWT Desktop API supports the BROWSE action (e.g., macOS, most Windows setups),
 * it is used directly. On platforms where it is not supported (e.g., Linux on Wayland),
 * this launcher falls back to [XdgBrowserLauncher] if `xdg-open` is available in the system PATH.
 */
class DefaultBrowserLauncher : BrowserLauncher {
    /**
     * Whether the Java AWT Desktop API supports opening URLs on the current platform.
     */
    private val isDesktopSupported: Boolean
        get() = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)

    /**
     * Fallback launcher using `xdg-open`, used when the Desktop API is not supported.
     */
    private val xdgFallback: XdgBrowserLauncher by lazy { XdgBrowserLauncher() }

    override val isValid: Boolean
        get() = isDesktopSupported || xdgFallback.isValid

    override fun launch(url: String) {
        when {
            isDesktopSupported -> {
                Desktop.getDesktop().browse(URI(url))
            }

            xdgFallback.isValid -> {
                xdgFallback.launch(url)
            }

            else -> {
                throw UnsupportedOperationException(
                    "Cannot open URL: neither the Desktop API nor xdg-open is available on this platform.",
                )
            }
        }
    }
}
