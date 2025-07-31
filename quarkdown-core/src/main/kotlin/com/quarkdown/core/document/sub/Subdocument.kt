package com.quarkdown.core.document.sub

import com.quarkdown.core.context.Context
import java.io.File

private const val ROOT_NAME = "index"
private const val UNIQUE_NAME_FORMAT = "%s@%d"

/**
 * A subdocument in a Quarkdown document is an independent unit that can be rendered separately,
 * can be linked from other documents, and can link to other subdocuments in order to create a graph of subdocuments,
 * available via [com.quarkdown.core.context.Context.subdocumentGraph].
 *
 * Additionally, a [com.quarkdown.core.rendering.PostRenderer] may adopt different strategies for rendering
 * different kinds of subdocuments.
 * For instance, an HTML post-renderer may render the root subdocument as a full HTML structure, with HTML, CSS and JS,
 * while a non-root subdocument may be just a single HTML resource linked from another document.
 */
sealed interface Subdocument {
    /**
     * The name of the subdocument, without any file extension.
     */
    val name: String

    /**
     * A unique name for the subdocument, which reduces the risk of name collisions.
     * This is a suitable name for output resources.
     */
    val uniqueName: String
        get() = name

    /**
     * The root [Subdocument], which is the main document of the Quarkdown pipeline.
     * This is always the entry point of the compilation process,
     * as it is the input content that is supplied to the pipeline.
     *
     * It is not bound to a file or resource, since it may be provided as a string or from other sources
     * that do not have a file representation.
     *
     * The root subdocument is the starting point of the subdocument graph,
     * available via [com.quarkdown.core.context.Context.subdocumentGraph].
     */
    data object Root : Subdocument {
        override val name: String
            get() = ROOT_NAME
    }

    /**
     * A [Subdocument] that is bound to a file or resource available at a specific path
     * and can be referenced by a link from the main document or another subdocument.
     * @param name the name of the subdocument, without extension
     * @param path the absolute path to the subdocument file or resource
     * @param workingDirectory the working directory to be used to resolve relative file paths within the subdocument.
     * Note that if this is `null`, then the pipeline's working directory should be used.
     * To get consistent results, rely on the context's [com.quarkdown.core.context.file.FileSystem.workingDirectory].
     * @param content the subdocument text content
     */
    data class Resource(
        override val name: String,
        val path: String,
        val workingDirectory: File? = null,
        val content: CharSequence,
    ) : Subdocument {
        override val uniqueName: String
            get() = UNIQUE_NAME_FORMAT.format(name, path.hashCode())
    }
}

/**
 * Returns the output file name for the subdocument, based on the context's options.
 * If the pipeline enforces minimal subdocument collisions ([com.quarkdown.core.pipeline.PipelineOptions.minimizeSubdocumentCollisions]),
 * [Subdocument.uniqueName] is returned, otherwise just [Subdocument.name], which is more human-readable but prone to collisions.
 * @param context the context that holds the pipeline options
 * @return the output file name for the subdocument
 */
fun Subdocument.getOutputFileName(context: Context): String =
    when {
        context.attachedPipeline?.options?.minimizeSubdocumentCollisions == true -> uniqueName
        else -> name
    }
