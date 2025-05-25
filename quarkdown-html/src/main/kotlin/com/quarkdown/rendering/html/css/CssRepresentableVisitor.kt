package com.quarkdown.rendering.html.css

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
import com.quarkdown.core.rendering.representable.RenderRepresentable
import com.quarkdown.core.rendering.representable.RenderRepresentableVisitor

/**
 * Producer of CSS representations of [RenderRepresentable]s.
 */
class CssRepresentableVisitor : RenderRepresentableVisitor<String> {
    /**
     * Name of the enum in kebab-case.
     * Example: `TOP_LEFT_CORNER` -> `top-left-corner`
     */
    private val Enum<*>.kebabCaseName: String
        get() = name.lowercase().replace("_", "-")

    override fun visit(color: Color) = with(color) { "rgba($red, $green, $blue, $alpha)" }

    override fun visit(size: Size) = "${size.value}${size.unit.symbol}" // e.g. 10px, 5cm, 2in

    override fun visit(sizes: Sizes) =
        with(sizes) {
            "${visit(top)} ${visit(right)} ${visit(bottom)} ${visit(left)}"
        }

    override fun visit(alignment: Table.Alignment) = alignment.kebabCaseName

    override fun visit(position: CaptionPosition) = position.kebabCaseName

    override fun visit(borderStyle: Container.BorderStyle) =
        when (borderStyle) {
            Container.BorderStyle.NORMAL -> "solid"
            else -> borderStyle.kebabCaseName
        }

    override fun visit(alignment: Container.Alignment) = alignment.kebabCaseName

    override fun visit(alignment: Container.FloatAlignment) = "inline-${alignment.kebabCaseName}"

    override fun visit(stackLayout: Stacked.Layout) =
        when (stackLayout) {
            is Stacked.Column -> "column"
            is Stacked.Row -> "row"
            is Stacked.Grid -> "grid"
        }

    override fun visit(alignment: Stacked.MainAxisAlignment) =
        when (alignment) {
            Stacked.MainAxisAlignment.START -> "flex-start"
            Stacked.MainAxisAlignment.END -> "flex-end"
            else -> alignment.kebabCaseName
        }

    override fun visit(alignment: Stacked.CrossAxisAlignment) =
        when (alignment) {
            Stacked.CrossAxisAlignment.START -> "flex-start"
            Stacked.CrossAxisAlignment.END -> "flex-end"
            else -> alignment.kebabCaseName
        }

    override fun visit(clip: Clipped.Clip) = clip.kebabCaseName

    override fun visit(quoteType: BlockQuote.Type) = quoteType.kebabCaseName

    override fun visit(boxType: Box.Type): String = boxType.kebabCaseName

    override fun visit(position: PageMarginPosition) = position.kebabCaseName

    override fun visit(transition: Transition.Style) = transition.kebabCaseName

    override fun visit(speed: Transition.Speed) = speed.kebabCaseName

    override fun visit(behavior: SlidesFragment.Behavior) =
        when (behavior) {
            SlidesFragment.Behavior.SHOW -> "fade-in"
            SlidesFragment.Behavior.HIDE -> "fade-out"
            SlidesFragment.Behavior.SEMI_HIDE -> "semi-fade-out"
            SlidesFragment.Behavior.SHOW_HIDE -> "fade-in-then-out"
        }

    override fun visit(size: TextTransformData.Size) = "size-${size.kebabCaseName}"

    override fun visit(weight: TextTransformData.Weight) = weight.kebabCaseName

    override fun visit(style: TextTransformData.Style) = style.kebabCaseName

    override fun visit(decoration: TextTransformData.Decoration) =
        when (decoration) {
            TextTransformData.Decoration.STRIKETHROUGH -> "line-through"
            TextTransformData.Decoration.UNDEROVERLINE -> "underline overline"
            TextTransformData.Decoration.ALL -> "underline overline line-through"
            else -> decoration.kebabCaseName
        }

    override fun visit(case: TextTransformData.Case) = case.kebabCaseName

    override fun visit(variant: TextTransformData.Variant) = variant.kebabCaseName
}

/**
 * Converts a [RenderRepresentable] to its CSS representation.
 * @see CssRepresentableVisitor
 */
val RenderRepresentable.asCSS: String
    get() = accept(CssRepresentableVisitor())
