package com.quarkdown.core.document.layout.caption

import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor

/**
 * Possible positions of captions, relative to the element they describe.
 * @see CaptionPositionInfo
 */
enum class CaptionPosition : RenderRepresentable {
    TOP,
    BOTTOM,
    ;

    override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
}
