package com.quarkdown.core.rendering

import com.quarkdown.core.rendering.html.HtmlPostRenderer

/**
 * Visitor for [PostRenderer]s
 * @param T output type of the visit operations
 */
interface PostRendererVisitor<T> {
    fun visit(postRenderer: HtmlPostRenderer): T
}
