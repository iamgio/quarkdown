package com.quarkdown.rendering.html

import com.quarkdown.installlayout.InstallLayout
import com.quarkdown.installlayout.get

actual fun defaultHtmlResourcesLayout(): InstallLayout.Html? = InstallLayout.get.htmlResources
