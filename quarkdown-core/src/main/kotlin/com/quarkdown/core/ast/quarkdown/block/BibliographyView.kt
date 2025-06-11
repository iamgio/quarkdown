package com.quarkdown.core.ast.quarkdown.block

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.bibliography.style.BibliographyStyle
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Renderable container of a [bibliography].
 * @param title title of the table of contents. If `null`, the default localized title is used
 * @param bibliography the bibliography to render
 * @param style the style to use for rendering the bibliography
 */
class BibliographyView(
    val title: InlineContent?,
    val bibliography: Bibliography,
    val style: BibliographyStyle,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
