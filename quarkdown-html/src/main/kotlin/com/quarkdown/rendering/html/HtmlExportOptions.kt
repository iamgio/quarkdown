package com.quarkdown.rendering.html

import com.quarkdown.installlayout.InstallLayout

/**
 * Options for exporting HTML artifacts.
 * @param resourcesLayout the install layout node for the `html/` subtree, used to locate
 *        themes, scripts, and third-party libraries. Defaults to the lazily resolved
 *        [InstallLayout.get] singleton. Pass `null` in tests to skip resource bundling.
 */
data class HtmlExportOptions(
    val resourcesLayout: InstallLayout.Html? = InstallLayout.get.htmlResources,
)
