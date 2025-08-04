package com.quarkdown.lsp.highlight

import org.eclipse.lsp4j.SemanticTokensParams

/**
 * Supplier for semantic tokens that highlight function calls.
 */
class FunctionCallTokensSupplier : SemanticTokensSupplier {
    override fun getTokens(
        params: SemanticTokensParams,
        text: String,
    ): List<SimpleTokenData> {
        val regex = Regex("""\.\w+""")
        return regex
            .findAll(text)
            .map { match ->
                SimpleTokenData(
                    range = match.range,
                    type = TokenType.FUNCTION_CALL,
                )
            }.toList()
    }
}
