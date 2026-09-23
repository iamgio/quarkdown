package com.quarkdown.core.document.size

import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor

/**
 * A collection of generic top, right, bottom and left [Size]s.
 */
data class Sizes(
    val top: Size,
    val right: Size,
    val bottom: Size,
    val left: Size,
) : RenderRepresentable {
    /**
     * Creates a [Sizes] object with the same [Size] for all sides.
     */
    constructor(all: Size) : this(all, all, all, all)

    /**
     * Creates a [Sizes] object with the same [Size] for vertical (top, bottom) and horizontal (left, right) sides.
     */
    constructor(vertical: Size, horizontal: Size) : this(vertical, horizontal, vertical, horizontal)

    override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)
}
