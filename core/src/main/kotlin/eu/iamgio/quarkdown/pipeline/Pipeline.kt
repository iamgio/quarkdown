package eu.iamgio.quarkdown.pipeline

import eu.iamgio.quarkdown.SystemProperties
import eu.iamgio.quarkdown.ast.Document
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.RendererFactory
import eu.iamgio.quarkdown.function.call.FunctionCallNodeExpander
import eu.iamgio.quarkdown.function.library.Library
import eu.iamgio.quarkdown.function.library.LibraryRegistrant
import eu.iamgio.quarkdown.isWrapOutputEnabled
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.acceptAll
import eu.iamgio.quarkdown.pipeline.error.PipelineException
import eu.iamgio.quarkdown.rendering.NodeRenderer
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
    private val libraries: Set<Library>,
    private val renderer: (RendererFactory, Context) -> NodeRenderer,
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
     * @see eu.iamgio.quarkdown.parser.BlockTokenParser
     * @see eu.iamgio.quarkdown.parser.InlineTokenParser
     */
    fun parse(tokens: List<Token>): Document {
        val parser = context.flavor.parserFactory.newParser(context)
        return Document(children = tokens.acceptAll(parser)).also {
            hooks?.afterParsing?.invoke(this, it)
        }
    }

    /**
     * Executes queued function calls and expands their content based on their output.
     */
    fun expandFunctionCalls(document: Document) {
        FunctionCallNodeExpander(context).expandAll()
        hooks?.afterExpanding?.invoke(this, document)
    }

    /**
     * Converts the AST to code for a target language.
     * If enabled by settings, the output code is wrapped in a template.
     * @param document root of the AST to render
     * @see eu.iamgio.quarkdown.rendering.NodeRenderer
     */
    private fun render(document: Document): CharSequence {
        val renderer = this.renderer(context.flavor.rendererFactory, context)
        val rendered = renderer.visit(document)

        hooks?.afterRendering?.invoke(this, rendered)

        // If enabled, the output code is wrapped in a template.
        val wrapped =
            if (SystemProperties.isWrapOutputEnabled) {
                renderer.wrap(rendered)
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
    fun execute(source: CharSequence) {
        val tokens = tokenize(source)
        val document = parse(tokens)
        expandFunctionCalls(document)
        render(document)
    }
}
