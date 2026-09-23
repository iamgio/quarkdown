package com.quarkdown.core.document.layout.page

import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor

/**
 * The side of a page in a paged document, used to apply distinct formatting
 * (e.g. mirrored margins) to left and right pages via [PageFormatInfo].
 */
enum class PageSide : RenderRepresentable {
    /** Left-hand (verso) pages in a spread. */
    LEFT,

    /** Right-hand (recto) pages in a spread. */
    RIGHT,
    ;

    override fun <T> accept(visitor: RenderRepresentableVisitor<T>): T = visitor.visit(this)
}
