package eu.iamgio.quarkdown.ast.quarkdown

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.document.slides.Transition
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A non-visible node that injects properties that affect the global configuration for slides documents.
 * @param centerVertically whether slides should be centered vertically
 * @param showControls whether navigation controls should be shown
 * @param transition global transition between slides
 */
data class SlidesConfigurationInitializer(
    val centerVertically: Boolean?,
    val showControls: Boolean?,
    val transition: Transition?,
) : Node {
    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
