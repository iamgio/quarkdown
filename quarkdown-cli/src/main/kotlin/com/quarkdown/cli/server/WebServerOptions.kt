package com.quarkdown.cli.server

import com.quarkdown.server.browser.BrowserLauncher
import java.io.File

/**
 * Options for the local web server.
 * @param port port to start the server on
 * @param targetFile file to serve
 * @param browserLauncher strategy to open the served file in the browser. If `null`, the file will not be opened
 * @param preferLivePreviewUrl if a browser launcher is provided, prefer to open the URL for live preview instead of the static file URL
 */
data class WebServerOptions(
    val port: Int,
    val targetFile: File,
    val browserLauncher: BrowserLauncher?,
    val preferLivePreviewUrl: Boolean,
)
