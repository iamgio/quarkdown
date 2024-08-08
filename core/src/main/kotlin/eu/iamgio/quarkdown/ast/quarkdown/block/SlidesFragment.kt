package eu.iamgio.quarkdown.ast.quarkdown.block

import eu.iamgio.quarkdown.ast.NestableNode
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor
import eu.iamgio.quarkdown.visitor.node.NodeVisitor

/**
 * A node that, when rendered in a `Slides` environment,
 * is displayed when the user attempts to go to the next slide.
 * Multiple fragments in the same slide are shown in order on distinct user interactions.
 * @param behavior visibility type of the fragment and how it reacts to user interactions
 */
data class SlidesFragment(
    val behavior: Behavior,
    override val children: List<Node>,
) : NestableNode {
    override fun <T> accept(visitor: NodeVisitor<T>): T = visitor.visit(this)

    /**
     * Possible visibility types of a [SlidesFragment].
     */
    enum class Behavior : RenderRepresentable {
        /**
         * Starts invisible, fades in on interaction.
         */
        SHOW,

        /**
         * Starts visible, fade out on interaction.
         */
        HIDE,

        /**
         * Starts visible, fade out to 50% on interaction.
         */
        SEMI_HIDE,

        /**
         * Starts invisible, fades in on interaction, then out on the next interaction.
         */
        SHOW_HIDE,
        ;

        override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
    }
}
