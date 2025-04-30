package com.quarkdown.core.rendering.representable

/**
 * A non-node element (usually a property of a node) that can be represented in a rendered document.
 *
 * For example, the node `Stacked` can render rows and columns with a specific alignment.
 * In an HTML rendered documeng, the alignment type `START` is represented by the `flex-start` CSS rule.
 * This conversion is done by a `RenderRepresentableVisitor`.
 */
interface RenderRepresentable {
    /**
     * Accepts a [visitor] to produce a representation of this element suitable for the rendered document.
     * @param visitor visitor to accept
     * @param T type of the rendered representation
     * @return rendered representation of this element
     */
    fun <T> accept(visitor: RenderRepresentableVisitor<T>): T
}
