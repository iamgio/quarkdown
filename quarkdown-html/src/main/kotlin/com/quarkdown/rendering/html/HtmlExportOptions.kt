package com.quarkdown.rendering.html

import java.io.File

/**
 * Options for exporting HTML artifacts.
 * @param libraryDirectory the filesystem directory containing third-party library files to bundle into the output,
 *        typically `lib/html` within the Quarkdown installation
 */
data class HtmlExportOptions(
    val libraryDirectory: File?,
)
