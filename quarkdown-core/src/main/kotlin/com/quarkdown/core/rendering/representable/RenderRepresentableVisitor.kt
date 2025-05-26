package com.quarkdown.core.rendering.representable

import com.quarkdown.core.ast.base.block.BlockQuote
import com.quarkdown.core.ast.base.block.Table
import com.quarkdown.core.ast.quarkdown.block.Box
import com.quarkdown.core.ast.quarkdown.block.Clipped
import com.quarkdown.core.ast.quarkdown.block.Container
import com.quarkdown.core.ast.quarkdown.block.SlidesFragment
import com.quarkdown.core.ast.quarkdown.block.Stacked
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData
import com.quarkdown.core.document.layout.caption.CaptionPosition
import com.quarkdown.core.document.layout.page.PageMarginPosition
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.document.size.Sizes
import com.quarkdown.core.document.slides.Transition
import com.quarkdown.core.misc.color.Color

/**
 * Visitor that produces representations of each [RenderRepresentable] subtype
 * suitable for the final rendered document.
 * @see com.quarkdown.core.rendering.html.CssRepresentableVisitor
 */
interface RenderRepresentableVisitor<T> {
    fun visit(color: Color): T

    fun visit(size: Size): T

    fun visit(sizes: Sizes): T

    fun visit(alignment: Table.Alignment): T

    fun visit(position: CaptionPosition): T

    fun visit(borderStyle: Container.BorderStyle): T

    fun visit(alignment: Container.Alignment): T

    fun visit(alignment: Container.TextAlignment): T

    fun visit(alignment: Container.FloatAlignment): T

    fun visit(stackLayout: Stacked.Layout): T

    fun visit(alignment: Stacked.MainAxisAlignment): T

    fun visit(alignment: Stacked.CrossAxisAlignment): T

    fun visit(clip: Clipped.Clip): T

    fun visit(quoteType: BlockQuote.Type): T

    fun visit(boxType: Box.Type): T

    fun visit(position: PageMarginPosition): T

    fun visit(transition: Transition.Style): T

    fun visit(speed: Transition.Speed): T

    fun visit(behavior: SlidesFragment.Behavior): T

    fun visit(size: TextTransformData.Size): T

    fun visit(weight: TextTransformData.Weight): T

    fun visit(style: TextTransformData.Style): T

    fun visit(decoration: TextTransformData.Decoration): T

    fun visit(case: TextTransformData.Case): T

    fun visit(variant: TextTransformData.Variant): T
}
