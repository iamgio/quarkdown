package com.quarkdown.server.browser

import java.nio.file.Path

/**
 * Browser launcher that uses a specific file system path to launch the browser.
 * @param path the file system path to the browser executable
 */
class PathBrowserLauncher(
    private val path: Path,
) : BrowserLauncher {
    override val isValid: Boolean
        get() = path.toFile().run { exists() && canExecute() }

    override fun launch(url: String) {
        val processBuilder = ProcessBuilder(path.toAbsolutePath().toString(), url)
        processBuilder.start()
    }
}
