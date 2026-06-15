package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.attributes.primitive.PrimitiveFunctionBackedNode
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.acceptAll
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData
import com.quarkdown.core.visitor.token.TokenVisitor

/**
 * Pipeline stage responsible for parsing tokens into an abstract syntax tree (AST).
 *
 * This stage takes a sequence of tokens (produced by the [LexingStage]) as input and
 * produces an [AstRoot] as output.
 *
 * The AST represents the hierarchical structure of the document and is used by
 * subsequent stages for further processing and rendering.
 *
 * Primitive Markdown nodes that implement [PrimitiveFunctionBackedNode] (e.g. headings) are wrapped here
 * into a [com.quarkdown.core.ast.quarkdown.FunctionCallNode] that delegates to their backing stdlib function. When the document
 * later wraps that function via `.extend`, the wrapper sees the node's properties as named
 * arguments and can override them; when it does not, the `FunctionCallNode` short-circuits and
 * renders the original node directly with no reflection overhead.
 */
object ParsingStage : PipelineStage<Sequence<Token>, AstRoot> {
    override val hook = PipelineHooks::afterParsing

    override fun process(
        input: Sequence<Token>,
        data: SharedPipelineData,
    ): AstRoot {
        val parser: TokenVisitor<Node> =
            data.context.flavor.parserFactory
                .newParser(data.context)

        val nodes =
            input
                .acceptAll(parser, data.context, isBlock = true)
                .toList()

        return AstRoot(children = nodes)
    }
}
