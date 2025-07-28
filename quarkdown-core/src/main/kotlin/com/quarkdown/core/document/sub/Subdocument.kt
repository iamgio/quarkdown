package com.quarkdown.core.document.sub

import java.io.File

private const val UNIQUE_NAME_FORMAT = "subdoc-%s@%d"

/**
 * A Quarkdown subdocument, a separate document file that can be rendered independently,
 * and is referenced by a link from the main document or another subdocument.
 * @param name the name of the subdocument, without extension
 * @param path the absolute path to the subdocument file or resource
 * @param workingDirectory the working directory to be used to resolve relative file paths
 * within the subdocument. Note that this is always `null` for the root subdocument, as it relies on
 * the pipeline's working directory. To get consistent results, rely on [com.quarkdown.core.context.file.FileSystem.workingDirectory].
 * @param content supplier of the subdocument content
 */
data class Subdocument(
    val name: String,
    val path: String,
    val workingDirectory: File? = null,
    val content: () -> CharSequence,
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
        val ROOT = Subdocument(name = "", path = "", content = { "" })
    }
}
