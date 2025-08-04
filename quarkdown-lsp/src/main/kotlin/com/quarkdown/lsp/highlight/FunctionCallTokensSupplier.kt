package com.quarkdown.lsp.highlight

import com.github.h0tk3y.betterParse.lexer.TokenMatch
import com.quarkdown.core.lexer.patterns.FunctionCallPatterns
import com.quarkdown.core.lexer.regex.StandardRegexLexer
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import org.eclipse.lsp4j.SemanticTokensParams

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

        // Quarkdown tokens.
        return lexer.tokenize().flatMap { token ->
            val result = token.data.walkerResult ?: return@flatMap emptyList()

            // Function call tokens are special, as they are processed by a walker
            // which produces nested tokens for each part of the call (e.g. name and parameters).
            // A semantic token is created for each eligible part.
            result.tokens
                .filter { it.offset <= result.endIndex }
                .mapNotNull { match ->
                    val start = token.data.position.first + match.offset
                    SimpleTokenData(
                        type = tokenToSemanticType(match) ?: return@mapNotNull null,
                        range = start until (start + match.length),
                    )
                }.toList()
        }
    }

    /**
     * Produces a semantic token type based on the part of the function call,
     * or `null` for no token.
     */
    private fun tokenToSemanticType(match: TokenMatch): TokenType? =
        when (match.type.name) {
            "begin" -> TokenType.FUNCTION_CALL_IDENTIFIER
            "identifier" -> if (match.tokenIndex == 1) TokenType.FUNCTION_CALL_IDENTIFIER else TokenType.FUNCTION_CALL_NAMED_PARAMETER
            "argumentNameDelimiter" -> TokenType.FUNCTION_CALL_NAMED_PARAMETER
            else -> null
        }
}
