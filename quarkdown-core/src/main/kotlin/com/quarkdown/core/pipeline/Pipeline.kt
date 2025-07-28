package com.quarkdown.core.pipeline

import com.quarkdown.core.ast.Document
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.function.call.FunctionCallNodeExpander
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.function.library.LibraryRegistrant
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.acceptAll
import com.quarkdown.core.pipeline.error.PipelineException
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.rendering.RenderingComponents
import com.quarkdown.core.rendering.wrap

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
    /**
     * A read-only version of the context of this pipeline.
     */
    val readOnlyContext: Context
        get() = context

    init {
        if (context.attachedPipeline == null) {
            Pipelines.attach(context, this)
            registerLibraries()
        }
    }

    private fun copy(context: MutableContext = this.context): Pipeline =
        Pipeline(
            context = context,
            options = options,
            libraries = libraries,
            renderer = renderer,
            hooks = hooks,
        )

    /**
     * Invokes the given [hook] of this pipeline and of all the registered libraries.
     * @param hook hook invocation
     * @see PipelineHooks
     * @see Library.hooks
     */
    private fun invokeHooks(hook: (PipelineHooks) -> Unit) {
        // Invoke the hook of this pipeline.
        hooks?.let { hook(it) }
        // Invoke the hook of all the registered libraries.
        libraries.forEach { it.hooks?.let(hook) }
    }

    /**
     * Registers [libraries] into [context].
     * @see LibraryRegistrant
     */
    private fun registerLibraries() {
        LibraryRegistrant(context).registerAll(this.libraries)
        invokeHooks { it.afterRegisteringLibraries(this, this.libraries) }
    }

    /**
     * Splits the source code into tokens.
     * @param source the source code to tokenize
     * @see com.quarkdown.core.lexer.Lexer
     */
    fun tokenize(source: CharSequence): List<Token> {
        val lexer = context.flavor.lexerFactory.newBlockLexer(source)
        return lexer.tokenize().also { tokens ->
            invokeHooks { it.afterLexing(this, tokens) }
        }
    }

    /**
     * Parses a list of tokens into an Abstract Syntax Tree.
     * @param tokens tokens to parse
     * @param context context to use for parsing, usually a child of this pipeline's context.
     *                If not provided, the pipeline's context is used.
     * @see com.quarkdown.core.parser.BlockTokenParser
     * @see com.quarkdown.core.parser.InlineTokenParser
     */
    fun parse(
        tokens: List<Token>,
        context: MutableContext = this.context,
    ): Document {
        val parser = context.flavor.parserFactory.newParser(context)
        return Document(children = tokens.acceptAll(parser)).also { document ->
            invokeHooks { it.afterParsing(this, document) }
        }
    }

    /**
     * Executes queued function calls and expands their content based on their output.
     */
    fun expandFunctionCalls(document: Document) {
        FunctionCallNodeExpander(context, options.errorHandler).expandAll()
        invokeHooks { it.afterExpanding(this, document) }
    }

    /**
     * Performs one full traversal of [document]'s AST.
     */
    private fun visitTree(document: Document) {
        context.flavor.treeIteratorFactory
            .default(context)
            .traverse(document)

        invokeHooks { it.afterTreeVisiting(this) }
    }

    /**
     * Converts the AST to code for a target language.
     * If enabled by settings, the output code is wrapped in a template.
     * @param document root of the AST to render
     * @param components node renderer and post-renderer to use
     * @see com.quarkdown.core.rendering.NodeRenderer
     */
    private fun render(
        document: Document,
        components: RenderingComponents,
    ): CharSequence {
        val rendered = components.nodeRenderer.visit(document)

        invokeHooks { it.afterRendering(this, rendered) }

        // If enabled, the output code is wrapped in a template.
        val wrapped =
            if (options.wrapOutput) {
                components.postRenderer.wrap(rendered)
            } else {
                rendered
            }

        invokeHooks { it.afterPostRendering(this, wrapped) }

        return wrapped
    }

    /**
     * Executes the pipeline and calls the given [hooks] after each stage.
     * This method does not return an output - use [hooks] to access the output of each stage instead.
     * @param source the source code to process and execute the stages onto
     * @throws PipelineException if an uncaught error occurs
     */
    fun execute(source: CharSequence): OutputResource? {
        val subdocument = context.subdocument
        val tokens = tokenize(source)
        val document = parse(tokens)
        context.attributes.root = document
        context.subdocumentGraph = context.subdocumentGraph.addVertex(subdocument)

        val renderer = this.renderer(context.flavor.rendererFactory, context).forSubdocument(subdocument)

        // The chosen renderer has its own preferred media storage options.
        // For example, HTML requires local media to be accessible from the file system,
        // hence local files must be stored and copied to the output directory.
        // It does not require remote media to be stored, as they are linked to from the web.
        // On the other hand, for example, LaTeX rendering (not yet supported) would require
        // all media to be stored locally, as it does not support remote media.
        //
        // The options are merged: if a rule is already set by the user, it is not overridden.
        // These options must be set before traversing the tree, as media is stored during it.
        context.options.mergeMediaStorageOptions(renderer.postRenderer.preferredMediaStorageOptions)

        // The user can further force override the media storage options.
        context.options.mergeMediaStorageOptions(options.mediaStorageOptionsOverrides)

        expandFunctionCalls(document)

        visitTree(document)

        val rendered = render(document, renderer)

        val subdocumentResources: Set<OutputResource> =
            context.subdocumentGraph
                .getNeighbors(subdocument)
                .mapNotNull { nextSubdocument ->
                    val subContext = context.fork(nextSubdocument)
                    copy(subContext).execute(nextSubdocument.content())
                }.toSet()

        // Resources generated by the post-renderer, possibly containing media resources as well.
        val resources: Set<OutputResource> =
            renderer.postRenderer.generateResources(rendered) + subdocumentResources

        // The output name of the final wrapped resource.
        val outputName = options.resourceName ?: context.documentInfo.name ?: "Untitled Quarkdown Document"

        return renderer.postRenderer.wrapResources(outputName, resources)
    }
}
