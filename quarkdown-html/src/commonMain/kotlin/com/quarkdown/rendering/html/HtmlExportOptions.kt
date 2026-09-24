package com.quarkdown.rendering.html

import com.quarkdown.installlayout.InstallLayout

/**
 * Options for exporting HTML artifacts.
 * @param resourcesLayout the install layout node for the `html/` subtree, used to locate
 *        themes, scripts, and third-party libraries. Defaults to the running installation's,
 *        or `null` where there is none, in which case no resource is bundled.
 */
data class HtmlExportOptions(
    val resourcesLayout: InstallLayout.Html? = defaultHtmlResourcesLayout(),
)
