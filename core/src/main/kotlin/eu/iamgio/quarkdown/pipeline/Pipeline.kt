package eu.iamgio.quarkdown.pipeline

import eu.iamgio.quarkdown.ast.Document
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.RendererFactory
import eu.iamgio.quarkdown.function.call.FunctionCallNodeExpander
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.library.LibraryRegistrant
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.acceptAll
import eu.iamgio.quarkdown.pipeline.error.PipelineException
import eu.iamgio.quarkdown.pipeline.output.OutputResource
import eu.iamgio.quarkdown.pipeline.output.OutputResourceGroup
import eu.iamgio.quarkdown.rendering.RenderingComponents
import eu.iamgio.quarkdown.rendering.wrap

/**
 * A representation of the sequential set of actions to perform in order to produce an output artifact from a raw source.
 * Each component of the pipeline takes an input from the output of the previous one.
 * @param context initial context data shared across this pipeline, which will is filled with useful information
 *                that are handed over to other stages of this pipeline.
 *                This allows gathering information on-the-fly without additional visits of the whole tree
 * @param libraries libraries to load and look up functions from
 * @param renderer supplier of the renderer implementation to use, produced by the flavor's [RendererFactory]
 *                 with the output attributes of the parser as an argument
 * @param hooks optional actions to run after each stage has been completed
 */
class Pipeline(
    private val context: MutableContext,
    val options: PipelineOptions,
    private val libraries: Set<Library>,
    private val renderer: (RendererFactory, Context) -> RenderingComponents,
    private val hooks: PipelineHooks? = null,
) {
    init {
        registerLibraries()
        Pipelines.attach(context, this)
    }

    /**
     * Registers [libraries] into [context].
     * @see LibraryRegistrant
     */
    private fun registerLibraries() {
        LibraryRegistrant(context).registerAll(this.libraries)
        hooks?.afterRegisteringLibraries?.invoke(this, this.libraries)
    }

    /**
     * Splits the source code into tokens.
     * @param source the source code to tokenize
     * @see eu.iamgio.quarkdown.lexer.Lexer
     */
    fun tokenize(source: CharSequence): List<Token> {
        val lexer = context.flavor.lexerFactory.newBlockLexer(source)
        return lexer.tokenize().also {
            hooks?.afterLexing?.invoke(this, it)
        }
    }

    /**
     * Parses a list of tokens into an Abstract Syntax Tree.
     * @param tokens tokens to parse
     * @param context context to use for parsing. If not provided, the pipeline's context is used.
     * @see eu.iamgio.quarkdown.parser.BlockTokenParser
     * @see eu.iamgio.quarkdown.parser.InlineTokenParser
     */
    fun parse(
        tokens: List<Token>,
        context: MutableContext = this.context,
    ): Document {
        val parser = context.flavor.parserFactory.newParser(context)
        return Document(children = tokens.acceptAll(parser)).also {
            hooks?.afterParsing?.invoke(this, it)
        }
    }

    /**
     * Executes queued function calls and expands their content based on their output.
     */
    fun expandFunctionCalls(document: Document) {
        FunctionCallNodeExpander(context, options.errorHandler).expandAll()
        hooks?.afterExpanding?.invoke(this, document)
    }

    /**
     * Perform one full traversal of [document]'s AST.
     */
    private fun visitTree(document: Document) {
        context.flavor.treeIteratorFactory
            .default(context)
            .traverse(document)

        hooks?.afterTreeVisiting?.invoke(this)
    }

    /**
     * Converts the AST to code for a target language.
     * If enabled by settings, the output code is wrapped in a template.
     * @param document root of the AST to render
     * @param components node renderer and post-renderer to use
     * @see eu.iamgio.quarkdown.rendering.NodeRenderer
     */
    private fun render(
        document: Document,
        components: RenderingComponents,
    ): CharSequence {
        val rendered = components.nodeRenderer.visit(document)

        hooks?.afterRendering?.invoke(this, rendered)

        // If enabled, the output code is wrapped in a template.
        val wrapped =
            if (options.wrapOutput) {
                components.postRenderer.wrap(rendered)
            } else {
                rendered
            }

        hooks?.afterPostRendering?.invoke(this, wrapped)

        return wrapped
    }

    /**
     * Executes the pipeline and calls the given [hooks] after each stage.
     * This method does not return an output - use [hooks] to access the output of each stage instead.
     * @param source the source code to process and execute the stages onto
     * @throws PipelineException if an uncaught error occurs
     */
    fun execute(source: CharSequence): OutputResource {
        val tokens = tokenize(source)
        val document = parse(tokens)
        context.attributes.root = document

        expandFunctionCalls(document)

        visitTree(document)

        val renderer = this.renderer(context.flavor.rendererFactory, context)
        val rendered = render(document, renderer)

        val resources = renderer.postRenderer.generateResources(rendered)
        return OutputResourceGroup(
            name = context.documentInfo.name ?: "Untitled Quarkdown Document",
            resources,
        )
    }
}
