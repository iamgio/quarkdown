package com.quarkdown.lsp.highlight

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.quarkdown.core.lexer.patterns.FunctionCallPatterns
import com.quarkdown.core.lexer.regex.StandardRegexLexer
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import org.eclipse.lsp4j.SemanticTokensParams

private const val FUNCTION_CALL_BEGIN_TOKEN_NAME = "begin"
private const val FUNCTION_CALL_IDENTIFIER_TOKEN_NAME = "identifier"
private const val FUNCTION_CALL_PARAMETER_NAME_DELIMITER_TOKEN_NAME = "argumentNameDelimiter"
private const val FUNCTION_CALL_ARGUMENT_CONTENT_TOKEN_NAME = "argContent"
private const val FUNCTION_CALL_INLINE_ARGUMENT_BEGIN_TOKEN_NAME = "argumentBegin"
private const val FUNCTION_CALL_INLINE_ARGUMENT_END_TOKEN_NAME = "argumentEnd"

/**
 * Supplier for semantic tokens that highlight function calls.
 */
class FunctionCallTokensSupplier : SemanticTokensSupplier {
    override fun getTokens(
        params: SemanticTokensParams,
        text: String,
    ): List<SimpleTokenData> {
        val pattern: TokenRegexPattern = FunctionCallPatterns().inlineFunctionCall
        val lexer = StandardRegexLexer(text, listOf(pattern))

        // When a function call argument is met, its content is enqueued
        // and its semantic tokens are extracted at the end of the main tokenization.
        val tokenizationQueue = mutableMapOf<Int, String>()

        val tokens =
            lexer.tokenize().flatMap { token ->
                val result = token.data.walkerResult ?: return@flatMap emptyList()

                // Function call are special tokens, as they are processed by a walker
                // which produces nested tokens for each part of the call (e.g. name and parameters).
                // A semantic token is created for each eligible part.
                result.tokens
                    .takeWhile { it.offset < result.endIndex }
                    .mapNotNull { match ->
                        val start = token.data.position.first + match.offset

                        // Enqueuing the tokenization of function call arguments.
                        if (match.type.name == FUNCTION_CALL_ARGUMENT_CONTENT_TOKEN_NAME) {
                            tokenizationQueue += start to match.text
                        }

                        SimpleTokenData(
                            type = tokenToSemanticType(match) ?: return@mapNotNull null,
                            range = start..(start + match.length),
                        )
                    }.toList()
            }

        return tokens + extractEnqueuedTokens(params, tokenizationQueue)
    }

    /**
     * Extracts semantic tokens from the enqueued function call arguments.
     * Each argument is tokenized separately, and the resulting tokens are adjusted
     * to account for their original offset in the source text.
     */
    private fun extractEnqueuedTokens(
        params: SemanticTokensParams,
        queue: Map<Int, String>,
    ): List<SimpleTokenData> =
        queue.flatMap { (offset, text) ->
            getTokens(params, text).map {
                val start = it.range.start + offset
                val end = it.range.last + offset
                it.copy(range = start..end)
            }
        }

    /**
     * Produces a semantic token type based on the part of the function call,
     * or `null` for no token.
     */
    private fun tokenToSemanticType(match: TokenMatch): TokenType? =
        when (match.type.name) {
            // .function
            // ^
            FUNCTION_CALL_BEGIN_TOKEN_NAME ->
                TokenType.FUNCTION_CALL_IDENTIFIER

            // .function parameter:{...}
            //  ^^^^^^^^ ^^^^^^^^^ (depending on the index)
            FUNCTION_CALL_IDENTIFIER_TOKEN_NAME ->
                if (match.tokenIndex == 1) TokenType.FUNCTION_CALL_IDENTIFIER else TokenType.FUNCTION_CALL_NAMED_PARAMETER

            // .function parameter:{...}
            //                    ^
            FUNCTION_CALL_PARAMETER_NAME_DELIMITER_TOKEN_NAME ->
                TokenType.FUNCTION_CALL_NAMED_PARAMETER

            // .function {...}
            //           ^   ^
            FUNCTION_CALL_INLINE_ARGUMENT_BEGIN_TOKEN_NAME, FUNCTION_CALL_INLINE_ARGUMENT_END_TOKEN_NAME ->
                TokenType.FUNCTION_CALL_INLINE_ARGUMENT_DELIMITER

            else -> null
        }
}
