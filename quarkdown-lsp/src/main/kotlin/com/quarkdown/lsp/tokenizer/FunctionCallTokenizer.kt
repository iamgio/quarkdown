package com.quarkdown.lsp.tokenizer

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.quarkdown.core.flavor.quarkdown.QuarkdownLexerFactory
import com.quarkdown.core.parser.walker.WalkerParsingResult
import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall
import com.quarkdown.core.util.offset

// Names of the tokens of the function call grammar (see core/FunctionCallGrammar).
private const val FUNCTION_CALL_BEGIN_TOKEN_NAME = "begin"
private const val FUNCTION_CALL_IDENTIFIER_TOKEN_NAME = "identifier"
private const val FUNCTION_CALL_CHAINING_SEPARATOR_NAME = "chainSeparator"
private const val FUNCTION_CALL_PARAMETER_NAME_DELIMITER_TOKEN_NAME = "argumentNameDelimiter"
private const val FUNCTION_CALL_ARGUMENT_CONTENT_TOKEN_NAME = "argContent"
private const val FUNCTION_CALL_INLINE_ARGUMENT_BEGIN_TOKEN_NAME = "argumentBegin"
private const val FUNCTION_CALL_INLINE_ARGUMENT_END_TOKEN_NAME = "argumentEnd"
private const val FUNCTION_CALL_INLINE_ARGUMENT_CONTENT_TOKEN_NAME = "argContent"

/**
 * Tokenizes function calls in text content.
 *
 * This class is responsible for parsing text to identify function calls and their components,
 * such as function names, parameters, and arguments. It uses a lexer to tokenize the text
 * and then processes the tokens to create [FunctionCall] objects.
 *
 * This is a lightweight approach to function call tokenization, more efficient than using a full parser.
 */
class FunctionCallTokenizer {
    /**
     * Parses the given text to extract function calls, tokenizing the input text and processing the tokens
     * to identify function calls and their components. It also handles nested function
     * calls by recursively processing function call arguments.
     * @param text the text to parse for function calls
     * @return the [FunctionCall]s found in the text
     */
    fun getFunctionCalls(text: String): List<FunctionCall> {
        val lexer = QuarkdownLexerFactory.newInlineFunctionCallLexer(text)

        // When a function call argument is met, its content is enqueued
        // and its tokens are extracted at the end of the main tokenization.
        val tokenizationQueue = mutableMapOf<Int, String>()

        val calls: List<FunctionCall> =
            lexer.tokenize().mapNotNull { token ->
                val result = token.data.walkerResult ?: return@mapNotNull null
                val start = token.data.position.first
                val end = start + result.endIndex

                var lastToken: FunctionCallToken.Type? = null
                // Function call are special tokens, as they are processed by a walker
                // which produces nested tokens for each part of the call (e.g. name and parameters).
                // A semantic token is created for each eligible part.
                val tokens: List<FunctionCallToken> =
                    result.tokens
                        .takeWhile { it.offset < result.endIndex }
                        .mapNotNull { match ->
                            val start = token.data.position.first + match.offset

                            // Enqueuing the tokenization of function call arguments.
                            if (match.type.name == FUNCTION_CALL_ARGUMENT_CONTENT_TOKEN_NAME) {
                                tokenizationQueue += start to match.text
                            }

                            lastToken = tokenMatchToType(match, lastToken)
                            FunctionCallToken(
                                type = lastToken ?: return@mapNotNull null,
                                range = start..(start + match.length),
                                lexeme = match.text,
                            )
                        }.toList()

                @Suppress("UNCHECKED_CAST")
                FunctionCall(
                    range = start..end,
                    tokens = tokens,
                    parserResult = result as WalkerParsingResult<WalkedFunctionCall>,
                )
            }

        return calls + extractEnqueuedTokens(tokenizationQueue)
    }

    /**
     * Extracts tokens from the enqueued function call arguments.
     *
     * Each argument is tokenized separately by recursively calling [getFunctionCalls],
     * and the resulting tokens are adjusted to account for their original offset in the source text.
     * This allows for proper handling of nested function calls within arguments.
     *
     * @param queue A map of offsets to argument content strings
     * @return A list of [FunctionCall] objects representing the function calls found in the arguments
     */
    private fun extractEnqueuedTokens(queue: Map<Int, String>): List<FunctionCall> =
        queue.flatMap { (offset, text) ->
            getFunctionCalls(text).map {
                it.copy(
                    range = it.range.offset(offset),
                    tokens =
                        it.tokens.map { token ->
                            token.copy(range = token.range.offset(offset))
                        },
                )
            }
        }

    /**
     * Maps a token match to a specific [FunctionCallToken.Type].
     *
     * This method determines the appropriate token type based on the token's name and position
     * in the function call. It handles various parts of a function call such as the beginning
     * marker, function name, parameter names, delimiters, and argument values.
     *
     * @param match The token match to map to a type
     * @return The corresponding [FunctionCallToken.Type], or `null` if the token should be ignored
     */
    private fun tokenMatchToType(
        match: TokenMatch,
        previous: FunctionCallToken.Type?,
    ): FunctionCallToken.Type? =
        when (match.type.name) {
            // .function
            // ^
            FUNCTION_CALL_BEGIN_TOKEN_NAME -> FunctionCallToken.Type.BEGIN

            // .function::function parameter:{...}
            //  ^^^^^^^^  ^^^^^^^^ ^^^^^^^^^ (depending on the last token)
            FUNCTION_CALL_IDENTIFIER_TOKEN_NAME ->
                when (previous) {
                    FunctionCallToken.Type.BEGIN, FunctionCallToken.Type.CHAINING_SEPARATOR ->
                        FunctionCallToken.Type.FUNCTION_NAME

                    else ->
                        FunctionCallToken.Type.PARAMETER_NAME
                }

            // .function::function
            //          ^^
            FUNCTION_CALL_CHAINING_SEPARATOR_NAME ->
                FunctionCallToken.Type.CHAINING_SEPARATOR

            // .function parameter:{...}
            //                    ^
            FUNCTION_CALL_PARAMETER_NAME_DELIMITER_TOKEN_NAME ->
                FunctionCallToken.Type.NAMED_PARAMETER_DELIMITER

            // .function {...}
            //           ^
            FUNCTION_CALL_INLINE_ARGUMENT_BEGIN_TOKEN_NAME ->
                FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN

            // .function {...}
            //               ^
            FUNCTION_CALL_INLINE_ARGUMENT_END_TOKEN_NAME ->
                FunctionCallToken.Type.INLINE_ARGUMENT_END

            // .function {...}
            //            ^^^
            FUNCTION_CALL_INLINE_ARGUMENT_CONTENT_TOKEN_NAME ->
                FunctionCallToken.Type.INLINE_ARGUMENT_VALUE

            else -> null
        }
}
