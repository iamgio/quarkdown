package com.quarkdown.core.pipeline.stages

import com.quarkdown.core.function.library.Library
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.pipeline.PipelineHooks
import com.quarkdown.core.pipeline.stage.PipelineStage
import com.quarkdown.core.pipeline.stage.SharedPipelineData

/**
 * Pipeline stage responsible for lexical analysis (tokenization) of the input text.
 *
 * This stage produces a list of tokens as output from the provided source text.
 * It uses the lexer factory from the context's flavor to create a block lexer that
 * processes the source text and breaks it down into tokens.
 *
 * The tokens produced by this stage are used by the [ParsingStage] to build an abstract syntax tree.
 *
 * @param source the raw input text to be tokenized
 */
class LexingStage(
    private val source: CharSequence,
) : PipelineStage<Set<Library>, List<Token>> {
    override val hook = PipelineHooks::afterLexing

    override fun process(
        input: Set<Library>,
        data: SharedPipelineData,
    ): List<Token> =
        data.context.flavor.lexerFactory
            .newBlockLexer(this.source)
            .tokenize()
}
