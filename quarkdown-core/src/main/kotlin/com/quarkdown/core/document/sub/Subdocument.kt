package com.quarkdown.core.document.sub

import java.io.Reader

/**
 * A Quarkdown subdocument, a separate document file that can be rendered independently,
 * and is referenced by a link from the main document or another subdocument.
 * @param name the name of the subdocument, without extension
 * @param reader supplier of a [Reader] to read the subdocument content
 */
data class Subdocument(
    val name: String,
    val reader: () -> Reader,
) {
    companion object {
        /**
         * The main document.
         * It has an empty file reference, and is used to represent the main document in the subdocument graph.
         */
        val ROOT = Subdocument(name = "", reader = { Reader.nullReader() })
    }
}
