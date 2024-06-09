package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.Clipped
import eu.iamgio.quarkdown.ast.Stacked
import eu.iamgio.quarkdown.document.page.PageMarginPosition
import eu.iamgio.quarkdown.document.page.Size
import eu.iamgio.quarkdown.document.page.Sizes
import eu.iamgio.quarkdown.document.slides.Transition
import eu.iamgio.quarkdown.misc.Color
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentable
import eu.iamgio.quarkdown.rendering.representable.RenderRepresentableVisitor

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

    override fun visit(size: Size) = size.toString()

    override fun visit(sizes: Sizes) = with(sizes) { "$top $right $bottom $left" }

    override fun visit(orientation: Stacked.Orientation) = orientation.kebabCaseName

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

    override fun visit(position: PageMarginPosition) = position.kebabCaseName

    override fun visit(transition: Transition.Style) = transition.kebabCaseName

    override fun visit(speed: Transition.Speed) = speed.kebabCaseName
}

/**
 * Converts a [RenderRepresentable] to its CSS representation.
 * @see CssRepresentableVisitor
 */
val RenderRepresentable.asCSS: String
    get() = accept(CssRepresentableVisitor())
