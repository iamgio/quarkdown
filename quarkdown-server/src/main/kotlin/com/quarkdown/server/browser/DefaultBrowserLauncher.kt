package com.quarkdown.server.browser

import java.awt.Desktop
import java.io.File
import java.net.URI
import kotlin.io.path.Path

/**
 * Launcher of a URL in the default system browser.
 *
 * On platforms where the Java AWT Desktop API supports the BROWSE action (e.g., macOS, most Windows setups),
 * it is used directly. On platforms where it is not supported (e.g., Linux on Wayland),
 * this launcher falls back to `xdg-open` via [PathBrowserLauncher] if it is available in the system PATH.
 */
class DefaultBrowserLauncher : BrowserLauncher {
    /**
     * Whether the Java AWT Desktop API supports opening URLs on the current platform.
     */
    private val isDesktopSupported: Boolean
        get() = Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)

    /**
     * A [PathBrowserLauncher] that uses `xdg-open` found in the system PATH,
     * or `null` if `xdg-open` is not available.
     * Used as a fallback when the Desktop API is not supported.
     */
    private val xdgFallback: PathBrowserLauncher? by lazy {
        System
            .getenv("PATH")
            ?.split(File.pathSeparator)
            ?.map { File(it, "xdg-open") }
            ?.firstOrNull { it.exists() && it.canExecute() }
            ?.let { PathBrowserLauncher(Path(it.absolutePath)) }
    }

    override val isValid: Boolean
        get() = isDesktopSupported || xdgFallback != null

    override fun launch(url: String) {
        if (isDesktopSupported) {
            Desktop.getDesktop().browse(URI(url))
        } else {
            xdgFallback?.launch(url)
                ?: throw UnsupportedOperationException(
                    "Cannot open URL: neither the Desktop API nor xdg-open is available on this platform.",
                )
        }
    }
}
