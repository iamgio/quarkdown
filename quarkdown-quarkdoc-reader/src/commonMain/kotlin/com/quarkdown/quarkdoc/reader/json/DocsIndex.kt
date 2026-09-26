package com.quarkdown.quarkdoc.reader.json

import com.quarkdown.quarkdoc.reader.DocsFunction
import kotlinx.serialization.Serializable

/**
 * File name of the documentation index within the documentation root directory.
 */
const val DOCS_INDEX_FILE_NAME = "docs-index.json"

/**
 * Pre-extracted documentation index, generated at documentation build time
 * by the Quarkdoc Dokka plugin, so that consumers (e.g. the language server)
 * can read function documentation without parsing HTML.
 *
 * The JSON format is defined by `docs-index.schema.json` in the Quarkdoc module's root.
 * @param functions the indexed functions
 */
@Serializable
data class DocsIndex(
    val functions: List<IndexedFunction>,
)

/**
 * A function entry of a [DocsIndex].
 * @param name the name of the documentation resource
 * @param moduleName the name of the Quarkdown module containing the function
 * @param function the function data, with parameter descriptions in Markdown
 * @param contentMarkdown the function's documentation content, in Markdown
 */
@Serializable
data class IndexedFunction(
    val name: String,
    val moduleName: String?,
    val function: DocsFunction,
    val contentMarkdown: String?,
)
