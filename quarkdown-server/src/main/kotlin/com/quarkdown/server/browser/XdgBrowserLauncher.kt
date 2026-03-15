package com.quarkdown.server.browser

import java.io.File
import kotlin.io.path.Path

/**
 * Browser launcher that uses `xdg-open` to open URLs on Linux systems.
 *
 * This is typically available on Linux systems with XDG-compliant desktop environments.
 * `xdg-open` delegates to the user's preferred browser.
 *
 * Delegates to [PathBrowserLauncher] if `xdg-open` is found in the system `PATH`.
 */
class XdgBrowserLauncher : BrowserLauncher {
    /**
     * The underlying [PathBrowserLauncher] pointing to the resolved `xdg-open` executable,
     * or `null` if `xdg-open` is not available in the system `PATH`.
     */
    private val delegate: PathBrowserLauncher? by lazy {
        System
            .getenv("PATH")
            ?.split(File.pathSeparator)
            ?.firstNotNullOfOrNull { directory ->
                File(directory, "xdg-open")
                    .takeIf { it.exists() && it.canExecute() }
            }?.let { PathBrowserLauncher(Path(it.absolutePath)) }
    }

    override val isValid: Boolean
        get() = delegate != null && delegate?.isValid == true

    override fun launch(url: String) {
        delegate?.launch(url)
            ?: throw UnsupportedOperationException(
                "Cannot open URL: xdg-open is not available in the system PATH.",
            )
    }
}
