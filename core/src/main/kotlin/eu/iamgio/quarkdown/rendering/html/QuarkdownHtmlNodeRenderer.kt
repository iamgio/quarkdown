package eu.iamgio.quarkdown.rendering.html

import eu.iamgio.quarkdown.ast.Aligned
import eu.iamgio.quarkdown.ast.Box
import eu.iamgio.quarkdown.ast.Clipped
import eu.iamgio.quarkdown.ast.FunctionCallNode
import eu.iamgio.quarkdown.ast.Math
import eu.iamgio.quarkdown.ast.MathSpan
import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.pipeline.output.ArtifactType
import eu.iamgio.quarkdown.pipeline.output.LazyOutputArtifact
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.rendering.tag.tagBuilder
import eu.iamgio.quarkdown.rendering.wrapper.RenderWrapper
import eu.iamgio.quarkdown.rendering.wrapper.TemplatePlaceholders

private const val BLOCK_MATH_FENCE = "__QD_BLOCK_MATH__"
private const val INLINE_MATH_FENCE = "__QD_INLINE_MATH__"

/**
 * A renderer for Quarkdown ([eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor]) nodes that exports their content into valid HTML code.
 * @param context additional information produced by the earlier stages of the pipeline
 */
class QuarkdownHtmlNodeRenderer(context: Context) : BaseHtmlNodeRenderer(context) {
    override fun createCodeWrapper() =
        RenderWrapper.fromResourceName("/render/quarkdown/html-wrapper.html")
            .value(TemplatePlaceholders.TITLE, context.documentInfo.name ?: "Quarkdown")
            .value(TemplatePlaceholders.LANGUAGE, "en") // TODO set language
            .conditional(TemplatePlaceholders.HAS_CODE, context.hasCode) // HighlightJS is initialized only if needed.
            .conditional(TemplatePlaceholders.HAS_MATH, context.hasMath) // MathJax is initialized only if needed.

    override fun generateResources(rendered: CharSequence): Set<OutputResource> =
        // A CSS theme file is added to the output resources.
        super.generateResources(rendered) +
            LazyOutputArtifact.internal(
                resource = "/render/quarkdown/theme.css",
                name = "theme",
                type = ArtifactType.CSS,
            )

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

    // Inline

    // Math is processed by the MathJax library which requires text delimiters instead of tags.
    override fun visit(node: MathSpan) = INLINE_MATH_FENCE + "$" + node.expression + "$" + INLINE_MATH_FENCE
}
