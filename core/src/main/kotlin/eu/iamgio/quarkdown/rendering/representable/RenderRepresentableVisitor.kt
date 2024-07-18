package eu.iamgio.quarkdown.rendering.representable

import eu.iamgio.quarkdown.ast.quarkdown.Clipped
import eu.iamgio.quarkdown.ast.quarkdown.SlidesFragment
import eu.iamgio.quarkdown.ast.quarkdown.Stacked
import eu.iamgio.quarkdown.ast.quarkdown.TextTransformData
import eu.iamgio.quarkdown.document.page.PageMarginPosition
import eu.iamgio.quarkdown.document.page.Size
import eu.iamgio.quarkdown.document.page.Sizes
import eu.iamgio.quarkdown.document.slides.Transition
import eu.iamgio.quarkdown.misc.Color

/**
 * Visitor that produces representations of each [RenderRepresentable] subtype
 * suitable for the final rendered document.
 * @see eu.iamgio.quarkdown.rendering.html.CssRepresentableVisitor
 */
interface RenderRepresentableVisitor<T> {
    fun visit(color: Color): T

    fun visit(size: Size): T

    fun visit(sizes: Sizes): T

    fun visit(orientation: Stacked.Orientation): T

    fun visit(alignment: Stacked.MainAxisAlignment): T

    fun visit(alignment: Stacked.CrossAxisAlignment): T

    fun visit(clip: Clipped.Clip): T

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
