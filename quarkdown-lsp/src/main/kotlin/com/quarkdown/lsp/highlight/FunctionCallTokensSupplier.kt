package com.quarkdown.lsp.highlight

import com.quarkdown.lsp.util.offsetToPosition
import org.eclipse.lsp4j.SemanticTokensParams

/**
 * Supplier for semantic tokens that highlight function calls.
 */
class FunctionCallTokensSupplier : SemanticTokensSupplier {
    override fun getTokens(
        params: SemanticTokensParams,
        text: String,
    ): List<SemanticTokenData> =
        buildList {
            val regex = Regex("""\.\w+""")
            regex.findAll(text).forEach { match ->
                val start = match.range.first
                val end = match.range.last + 1

                val pos = offsetToPosition(text, start)
                val length = end - start

                this += SemanticTokenData(pos.line, pos.character, length, 0, 0)
            }
        }
}
