package com.quarkdown.lsp.highlight

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

        return lexer.tokenize().mapNotNull { token ->
            val result = token.data.walkerResult ?: return@mapNotNull null
            val start = token.data.position.first
            val end = start + result.endIndex
            SimpleTokenData(
                range = start until end,
                type = TokenType.FUNCTION_CALL,
            )
        }
    }
}
