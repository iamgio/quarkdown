package com.quarkdown.lsp

import com.quarkdown.lsp.cache.DocumentCache

/**
 * An immutable representation of a text document that the user is working on.
 * @param text the content of the document
 * @param cache precomputed cache for various attributes of the document, or `null` if missing
 * @param setActive function to overwrite the active document with this instance for the same source URI
 */
data class TextDocument(
    val text: String,
    val cache: DocumentCache? = null,
    val setActive: TextDocument.() -> Unit = {},
) {
    /**
     * The pre-computed cache for the document if it exists; otherwise, computes and updates it
     * invoking [setActive].
     */
    val cacheOrCompute: DocumentCache
        get() {
            val cache = this.cache ?: DocumentCache.compute(this)
            this.updateCache(cache).setActive()
            return cache
        }

    /**
     * @return a copy of this document with the provided new cache.
     */
    fun updateCache(newCache: DocumentCache): TextDocument = this.copy(cache = newCache)

    /**
     * @return a copy of this document with the cache invalidated (set to `null`).
     */
    fun invalidateCache(): TextDocument = this.copy(cache = null)
}
