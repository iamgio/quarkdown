package com.quarkdown.core.document.sub

import java.io.Reader

private const val UNIQUE_NAME_FORMAT = "subdoc-%s@%d"

/**
 * A Quarkdown subdocument, a separate document file that can be rendered independently,
 * and is referenced by a link from the main document or another subdocument.
 * @param name the name of the subdocument, without extension
 * @param path the absolute path to the subdocument file or resource
 * @param reader supplier of a [Reader] to read the subdocument content
 */
data class Subdocument(
    val name: String,
    val path: String,
    val reader: () -> Reader,
) {
    /**
     * A unique name for the subdocument, which reduces the risk of name collisions.
     * This is a suitable name for output resources.
     */
    val uniqueName: String
        get() = UNIQUE_NAME_FORMAT.format(name, path.hashCode())

    companion object {
        /**
         * The main document.
         * It has an empty file reference, and is used to represent the main document in the subdocument graph.
         */
        val ROOT = Subdocument(name = "", path = "", reader = { Reader.nullReader() })
    }
}
