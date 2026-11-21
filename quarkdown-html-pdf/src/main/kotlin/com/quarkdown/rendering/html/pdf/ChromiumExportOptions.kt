package com.quarkdown.rendering.html.pdf

import java.io.File

/**
 * Options for exporting HTML documents through a Chromium-family browser via [ChromiumExportScript].
 * @param outputDirectory directory to save the exported file in
 * @param chromePath path to the Chromium-family browser executable
 * @param noSandbox whether to disable the Chromium sandbox during the export. Potentially unsafe
 */
data class ChromiumExportOptions(
    val outputDirectory: File,
    val chromePath: String,
    val noSandbox: Boolean = false,
)
