package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.ast.AstRoot
import com.quarkdown.core.ast.Node
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.acceptAll
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData
import com.quarkdown.core.visitor.token.TokenVisitor

/**
 * Pipeline stage responsible for parsing tokens into an abstract syntax tree (AST).
 *
 * This stage takes a list of tokens (produced by the [LexingStage]) as input and
 * produces an [AstRoot] as output.
 *
 * The AST represents the hierarchical structure of the document and is used by
 * subsequent stages for further processing and rendering.
 */
object ParsingStage : PipelineStage<List<Token>, AstRoot> {
    override val hook = PipelineHooks::afterParsing

    override fun process(
        input: List<Token>,
        data: SharedPipelineData,
    ): AstRoot {
        val parser: TokenVisitor<Node> =
            data.context.flavor.parserFactory
                .newParser(data.context)

        return AstRoot(children = input.acceptAll(parser))
    }
}
