package eu.iamgio.quarkdown.rendering

import eu.iamgio.quarkdown.rendering.html.HtmlPostRenderer

/**
 * Visitor for [PostRenderer]s
 * @param T output type of the visit operations
 */
interface PostRendererVisitor<T> {
    fun visit(postRenderer: HtmlPostRenderer): T
}
