package com.quarkdown.quarkdoc.reader

/**
 *
 */
interface ContentExtractor {
    /**
     * @return the extracted main content, if available
     */
    fun extractContent(): String?
}
