package com.quarkdown.lsp.cache

import com.quarkdown.lsp.TextDocument
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallTokenizer

/**
 * Cache for various precomputed attributes of a [TextDocument].
 *
 * This allows avoiding repeated computation of expensive operations like tokenization
 * when the document content hasn't changed.
 *
 * @param functionCalls the list of function calls identified and tokenized in the document
 */
data class DocumentCache(
    val functionCalls: List<FunctionCall>,
) {
    companion object {
        /**
         * Computes a [DocumentCache] for the given [document] by tokenizing its content.
         * @param document the text document to compute the cache for
         * @return a new [DocumentCache] instance with computed attributes
         */
        fun compute(document: TextDocument): DocumentCache {
            val functionCalls = FunctionCallTokenizer().getFunctionCalls(document.text)
            return DocumentCache(
                functionCalls = functionCalls,
            )
        }
    }
}
