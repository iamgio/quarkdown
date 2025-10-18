package com.quarkdown.core.document.layout.page

import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor

/**
 * Position of a margin box on a page.
 * @param whenOnRightPage if not `null`, the position to use when the margin content is on a right page.
 *                        Typically used for mirror positions (outside/inside).
 * @param whenOnLeftPage if not `null`, the position to use when the margin content is on a left page.
 *                       Typically used for mirror positions (outside/inside).
 * @see com.quarkdown.core.document.layout.page.PageMarginPosition
 */
enum class PageMarginPosition(
    private val whenOnLeftPage: PageMarginPosition? = null,
    private val whenOnRightPage: PageMarginPosition? = null,
) : RenderRepresentable {
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

    // Mirror outside positions.
    TOP_OUTSIDE_CORNER(whenOnLeftPage = TOP_LEFT_CORNER, whenOnRightPage = TOP_RIGHT_CORNER),
    TOP_OUTSIDE(whenOnLeftPage = TOP_LEFT, whenOnRightPage = TOP_RIGHT),
    BOTTOM_OUTSIDE_CORNER(whenOnLeftPage = BOTTOM_LEFT_CORNER, whenOnRightPage = BOTTOM_RIGHT_CORNER),
    BOTTOM_OUTSIDE(whenOnLeftPage = BOTTOM_LEFT, whenOnRightPage = BOTTOM_RIGHT),

    // Mirror inside positions.
    TOP_INSIDE_CORNER(whenOnLeftPage = TOP_RIGHT_CORNER, whenOnRightPage = TOP_LEFT_CORNER),
    TOP_INSIDE(whenOnLeftPage = TOP_RIGHT, whenOnRightPage = TOP_LEFT),
    BOTTOM_INSIDE_CORNER(whenOnLeftPage = BOTTOM_RIGHT_CORNER, whenOnRightPage = BOTTOM_LEFT_CORNER),
    BOTTOM_INSIDE(whenOnLeftPage = BOTTOM_RIGHT, whenOnRightPage = BOTTOM_LEFT),
    ;

    /**
     * The position to use when rendering the margin content on a left page.
     * @return [whenOnLeftPage] if specified, otherwise the fixed position itself.
     */
    val forLeftPage: PageMarginPosition
        get() = this.whenOnLeftPage ?: this

    /**
     * The position to use when rendering the margin content on a right page.
     * @return [whenOnRightPage] if specified, otherwise the fixed position itself.
     */
    val forRightPage: PageMarginPosition
        get() = this.whenOnRightPage ?: this

    override fun <T> accept(visitor: RenderRepresentableVisitor<T>) = visitor.visit(this)
}
