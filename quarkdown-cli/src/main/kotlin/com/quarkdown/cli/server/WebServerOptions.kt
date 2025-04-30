package com.quarkdown.cli.server

import com.quarkdown.server.browser.BrowserLauncher
import java.io.File

/**
 * Options for the local web server.
 * @param port port to start the server on
 * @param targetFile file to serve
 * @param browserLauncher strategy to open the served file in the browser. If `null`, the file will not be opened.
 */
data class WebServerOptions(
    val port: Int,
    val targetFile: File,
    val browserLauncher: BrowserLauncher?,
)
