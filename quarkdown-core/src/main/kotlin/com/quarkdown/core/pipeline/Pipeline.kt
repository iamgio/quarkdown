package com.quarkdown.core.pipeline

import com.quarkdown.core.ast.Document
import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.function.call.FunctionCallNodeExpander
import com.quarkdown.core.function.library.Library
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.acceptAll
import com.quarkdown.core.pipeline.error.PipelineException
import com.quarkdown.core.pipeline.output.OutputResource
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData
import com.quarkdown.core.pipeline.stage.execute
import com.quarkdown.core.pipeline.stage.then
import com.quarkdown.core.pipeline.stages.AttachmentStage
import com.quarkdown.core.pipeline.stages.AttributesUpdateStage
import com.quarkdown.core.pipeline.stages.FunctionCallExpansionStage
import com.quarkdown.core.pipeline.stages.LexingStage
import com.quarkdown.core.pipeline.stages.LibrariesRegistrationStage
import com.quarkdown.core.pipeline.stages.ParsingStage
import com.quarkdown.core.pipeline.stages.PostRenderingStage
import com.quarkdown.core.pipeline.stages.RenderingStage
import com.quarkdown.core.pipeline.stages.ResourceGenerationStage
import com.quarkdown.core.pipeline.stages.TreeTraversalStage
import com.quarkdown.core.rendering.RenderingComponents

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
    val libraries: Set<Library>,
    private val renderer: (RendererFactory, Context) -> RenderingComponents,
    val hooks: PipelineHooks? = null,
) {
    private val renderingComponents: RenderingComponents by lazy { renderer(context.flavor.rendererFactory, context) }

    private val chain: PipelineStage<Unit, Set<OutputResource>>
        get() =
            AttachmentStage then
                LibrariesRegistrationStage then
                LexingStage then
                ParsingStage then
                AttributesUpdateStage(preferredMediaStorageOptions = renderingComponents.postRenderer.preferredMediaStorageOptions) then
                FunctionCallExpansionStage then
                TreeTraversalStage then
                RenderingStage(renderingComponents.nodeRenderer) then
                PostRenderingStage(renderingComponents.postRenderer) then
                ResourceGenerationStage(renderingComponents.postRenderer)

    /**
     * A read-only version of the context of this pipeline.
     */
    val readOnlyContext: Context
        get() = context

    fun copy(context: MutableContext = this.context): Pipeline =
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
     * Executes the pipeline and calls the given [hooks] after each stage.
     * @param source the source code to process and execute the stages onto
     * @throws PipelineException if an uncaught error occurs
     * @return a set of output resources generated by the pipeline
     */
    fun executeUnwrapped(source: CharSequence): Set<OutputResource> {
        val sharedData =
            SharedPipelineData(
                pipeline = this,
                context = context,
                source = source,
            )
        return chain.execute(sharedData)
    }

    /**
     * Executes the pipeline and calls the given [hooks] after each stage.
     * @param source the source code to process and execute the stages onto
     * @throws PipelineException if an uncaught error occurs
     * @return a single output resource that wraps all the resources generated by the pipeline.
     */
    fun execute(source: CharSequence): OutputResource? {
        val resources = executeUnwrapped(source)

        // The output name of the final wrapped resource.
        val outputName = options.resourceName ?: context.documentInfo.name ?: "Untitled Quarkdown Document"

        return renderingComponents.postRenderer.wrapResources(outputName, resources)
    }
}
