package com.quarkdown.core.ast.quarkdown.bibliography

import com.quarkdown.core.ast.Node
import com.quarkdown.core.bibliography.Bibliography
import com.quarkdown.core.bibliography.style.BibliographyStyle
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * Renderable container of a [bibliography].
 * @param bibliography the bibliography to render
 * @param style the style to use for rendering the bibliography
 */
class BibliographyView(
    val bibliography: Bibliography,
    val style: BibliographyStyle,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)
}
