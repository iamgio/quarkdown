@file:QModule

package com.quarkdown.stdlib

import com.quarkdown.core.ast.quarkdown.inline.IconImage
import com.quarkdown.core.function.value.wrappedAsValue
import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.processor.annotation.QModule

/**
 * Shows a pixel-perfect icon, looked up from the icon library by its name.
 *
 * Note: icon libraries and names are dependent on the renderer.
 * No validation is performed at compile time, and missing icons may not be rendered or rendered incorrectly.
 *
 * In HTML (and HTML-PDF) rendering, the [Bootstrap Icons](https://icons.getbootstrap.com/#icons) library is used.
 *
 * @param name the name of the icon
 */
@QFunction
fun icon(name: String) = IconImage(name).wrappedAsValue()
