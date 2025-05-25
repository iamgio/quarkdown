package com.quarkdown.core.document.layout.page

import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor

/**
 * Position of a margin box on a page.
 */
enum class PageMarginPosition : RenderRepresentable {
    // See: https://pagedjs.org/documentation/7-generated-content-in-margin-boxes/
    // Ordered by position, clockwise from the top left corner.
    TOP_LEFT_CORNER,
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    TOP_RIGHT_CORNER,
    RIGHT_TOP,
    RIGHT_MIDDLE,
    RIGHT_BOTTOM,
    BOTTOM_RIGHT_CORNER,
    BOTTOM_RIGHT,
    BOTTOM_CENTER,
    BOTTOM_LEFT,
    BOTTOM_LEFT_CORNER,
    LEFT_BOTTOM,
    LEFT_MIDDLE,
    LEFT_TOP,
    ;

    override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)
}
