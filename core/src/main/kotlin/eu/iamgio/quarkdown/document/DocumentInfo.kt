package eu.iamgio.quarkdown.document

/**
 * Mutable information about the final artifact.
 * This data is mutated by library functions `.docname`, `.docauthor`, etc.
 * @param name name of the document, if specified
 * @param author author of the document, if specified
 * @param theme theme of the document, if specified
 */
data class DocumentInfo(
    var name: String? = null,
    var author: String? = null,
    var theme: String? = null,
)
