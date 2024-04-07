package eu.iamgio.quarkdown.pipeline

import eu.iamgio.quarkdown.SystemProperties
import eu.iamgio.quarkdown.ast.Document
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.RendererFactory
import eu.iamgio.quarkdown.function.AstFunctionCallExpander
import eu.iamgio.quarkdown.isWrapOutputEnabled
import eu.iamgio.quarkdown.lexer.acceptAll
import eu.iamgio.quarkdown.rendering.NodeRenderer
import eu.iamgio.quarkdown.rendering.wrap

/**
 * A representation of the sequential set of actions to perform in order to produce an output artifact from a raw source.
 * Each component of the pipeline takes an input from the output of the previous one.
 * @param components strategies to use for each main stage of the pipeline
 * @param hooks optional actions to run after each stage has been completed
 */
class Pipeline(
    val components: PipelineComponents,
    private val hooks: PipelineHooks? = null,
) {
    /**
     * @param source raw input source to compile
     * @param flavor Markdown flavor used for this pipeline. It specifies how to produce the needed components
     * @param renderer supplier of the renderer implementation to use, produced by the [flavor]'s [RendererFactory]
     *                 with the output attributes of the parser as an argument
     * @param context initial context data to hand to the parser, which will fill it further with useful information
     *                and hand it over to the renderer.
     *                This allows gathering information on-the-fly without additional visits of the whole tree
     * @param hooks optional actions to run after each stage has been completed
     */
    constructor(
        source: CharSequence,
        flavor: MarkdownFlavor,
        renderer: (RendererFactory, Context) -> NodeRenderer,
        context: MutableContext = MutableContext(),
        hooks: PipelineHooks? = null,
    ) : this(
        PipelineComponents(
            flavor.lexerFactory.newBlockLexer(source),
            flavor.parserFactory.newParser(context),
            AstFunctionCallExpander(context),
            renderer(flavor.rendererFactory, context),
        ),
        hooks,
    )

    /**
     * Executes the pipeline and calls the given [hooks] after each stage.
     */
    fun execute() {
        // Lexing.
        val tokens = components.lexer.tokenize()
        hooks?.afterLexing?.invoke(this, tokens)

        // Parsing.
        val document = Document(children = tokens.acceptAll(components.parser))
        hooks?.afterParsing?.invoke(this, document)

        // TODO load libraries

        // Executes queued function calls and expands their content based on their output.
        components.functionCallExpander.expandAll()
        hooks?.afterExpanding?.invoke(this, document)

        // Rendering.
        val rendered = components.renderer.visit(document)
        hooks?.afterRendering?.invoke(this, rendered)

        // Post rendering.

        // If enabled, the output code is wrapped in a template.
        val output =
            if (SystemProperties.isWrapOutputEnabled) {
                components.renderer.wrap(rendered)
            } else {
                rendered
            }

        hooks?.afterPostRendering?.invoke(this, output)
    }
}
