package com.quarkdown.rendering.html

import com.quarkdown.installlayout.InstallLayout

/**
 * @return the `html/` install layout node of the running installation, if the platform has one
 */
expect fun defaultHtmlResourcesLayout(): InstallLayout.Html?
