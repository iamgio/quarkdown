package eu.iamgio.quarkdown.document

import eu.iamgio.quarkdown.document.page.PageFormatInfo

/**
 * Mutable information about the final artifact.
 * This data is mutated by library functions `.docname`, `.docauthor`, etc.
 * @param type type of the document
 * @param name name of the document, if specified
 * @param author author of the document, if specified
 * @param theme theme of the document, if specified
 */
data class DocumentInfo(
    var type: DocumentType = DocumentType.PLAIN,
    var name: String? = null,
    var author: String? = null,
    var theme: String? = null,
    val pageFormat: PageFormatInfo = PageFormatInfo(),
)
