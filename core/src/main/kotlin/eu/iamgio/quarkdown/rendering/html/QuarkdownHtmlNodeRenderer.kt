package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.Aligned
import eu.iamgio.quarkdown.ast.Box
import eu.iamgio.quarkdown.ast.Clipped
import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Math
import eu.iamgio.quarkdown.ast.MathSpan
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.ast.PageBreak
import eu.iamgio.quarkdown.ast.PageCounterInitializer
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.rendering.tag.buildTag
import eu.iamgio.quarkdown.rendering.tag.tagBuilder

private const val BLOCK_MATH_FENCE = "__QD_BLOCK_MATH__"
private const val INLINE_MATH_FENCE = "__QD_INLINE_MATH__"

/**
 * A renderer for Quarkdown ([eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param context additional information produced by the earlier stages of the pipeline
 */
class QuarkdownHtmlNodeRenderer(context: Context) : BaseHtmlNodeRenderer(context) {
    /**
     * A `<div class="styleClass">...</div>` tag.
     */
    private fun div(
        styleClass: String,
        init: HtmlTagBuilder.() -> Unit,
    ) = tagBuilder("div", init = init)
        .attribute("class", styleClass)
        .build()

    /**
     * A `<div class="styleClass">children</div>` tag.
     */
    private fun div(
        styleClass: String,
        children: List<Node>,
    ) = div(styleClass) { +children }

    // Quarkdown node rendering

    // The function was already expanded by previous stages: its output nodes are stored in its children.
    override fun visit(node: FunctionCallNode): CharSequence = node.children.joinToString(separator = "") { it.accept(this) }

    // Block

    // An empty div that acts as a page break.
    override fun visit(node: PageBreak) = div("page-break") {}

    // Math is processed by the MathJax library which requires text delimiters instead of tags.
    override fun visit(node: Math) = BLOCK_MATH_FENCE + "$" + node.expression + "$" + BLOCK_MATH_FENCE

    override fun visit(node: Aligned) = div("align align-" + node.alignment.name.lowercase(), node.children)

    override fun visit(node: Clipped) = div("clip-" + node.clip.name.lowercase(), node.children)

    override fun visit(node: Box) =
        div("box") {
            if (node.title != null) {
                tag("header") {
                    tag("h5") {
                        +node.title
                    }
                }
            }
            +node.children
        }

    override fun visit(node: PageCounterInitializer) =
        buildTag("script") {
            val property = "--page-margin-${node.position.asCSS}-content"
            val content = node.text("\"counter(page)\"", "\"counter(pages)\"")
            +"document.documentElement.style.setProperty('$property', '\"$content\"');"
        }

    // Inline

    // Math is processed by the MathJax library which requires text delimiters instead of tags.
    override fun visit(node: MathSpan) = INLINE_MATH_FENCE + "$" + node.expression + "$" + INLINE_MATH_FENCE
}
