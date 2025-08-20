package com.quarkdown.lsp.highlight.function

import com.quarkdown.lsp.highlight.SemanticTokensSupplier
import com.quarkdown.lsp.highlight.SimpleTokenData
import com.quarkdown.lsp.highlight.TokenType
import com.quarkdown.lsp.tokenizer.FunctionCallToken
import com.quarkdown.lsp.tokenizer.FunctionCallTokenizer
import org.eclipse.lsp4j.SemanticTokensParams

/**
 * Supplier for semantic tokens that highlight function calls.
 */
class FunctionCallTokensSupplier : SemanticTokensSupplier {
    override fun getTokens(
        params: SemanticTokensParams,
        text: String,
    ): Iterable<SimpleTokenData> =
        FunctionCallTokenizer()
            .getFunctionCalls(text)
            .asSequence()
            .flatMap { it.tokens }
            .map { it.toSimpleTokenData() }
            .filterNotNull()
            .toList()

    /**
     * Converts a [com.quarkdown.lsp.tokenizer.FunctionCallToken] to a [SimpleTokenData] suitable for semantic highlighting,
     * or returns `null` if the token type does not correspond to a highlightable token.
     */
    private fun FunctionCallToken.toSimpleTokenData(): SimpleTokenData? {
        val type: TokenType? =
            when (type) {
                FunctionCallToken.Type.BEGIN, FunctionCallToken.Type.FUNCTION_NAME -> TokenType.FUNCTION_CALL_IDENTIFIER
                FunctionCallToken.Type.PARAMETER_NAME, FunctionCallToken.Type.NAMED_PARAMETER_DELIMITER -> TokenType.FUNCTION_CALL_NAMED_PARAMETER
                FunctionCallToken.Type.INLINE_ARGUMENT_BEGIN, FunctionCallToken.Type.INLINE_ARGUMENT_END -> TokenType.FUNCTION_CALL_INLINE_ARGUMENT_DELIMITER
                FunctionCallToken.Type.INLINE_ARGUMENT_VALUE -> ValueQualifier.getTokenType(lexeme.trim())
                FunctionCallToken.Type.BODY_ARGUMENT -> null
            }
        return SimpleTokenData(
            type = type ?: return null,
            range = range,
        )
    }
}
