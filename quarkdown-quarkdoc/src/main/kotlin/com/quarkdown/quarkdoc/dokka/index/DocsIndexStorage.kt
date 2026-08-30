package com.quarkdown.quarkdoc.dokka.index

import com.quarkdown.quarkdoc.reader.json.IndexedFunction

/**
 * Storage of function data collected from the Dokka model by [DocsIndexCollectorTransformer],
 * later joined with rendered content and written out by [DocsIndexWriterPostAction].
 */
object DocsIndexStorage {
    private val functions = mutableListOf<IndexedFunction>()

    /**
     * Registers a collected function.
     */
    fun add(function: IndexedFunction) {
        functions += function
    }

    /**
     * @return the collected function of the given [name] within [moduleName], if any
     */
    fun find(
        moduleName: String?,
        name: String,
    ): IndexedFunction? = functions.find { it.moduleName == moduleName && it.name == name }

    fun clear() {
        functions.clear()
    }
}
