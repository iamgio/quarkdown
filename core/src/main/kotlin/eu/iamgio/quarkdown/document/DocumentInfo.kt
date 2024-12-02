package eu.iamgio.quarkdown.document

import eu.iamgio.quarkdown.document.numbering.DocumentNumbering
import eu.iamgio.quarkdown.document.page.PageFormatInfo
import eu.iamgio.quarkdown.localization.Locale

/**
 * Mutable information about the final artifact.
 * This data is mutated by library functions `.docname`, `.docauthor`, etc.
 * @param type type of the document
 * @param name name of the document, if specified
 * @param authors authors of the document, if specified
 * @param theme theme of the document, if specified
 * @param locale language of the document
 * @param numbering formats to apply to element numbering across the document
 * @param pageFormat format of the pages of the document
 */
data class DocumentInfo(
    var type: DocumentType = DocumentType.PLAIN,
    var name: String? = null,
    val authors: MutableList<DocumentAuthor> = mutableListOf(),
    var locale: Locale? = null,
    var numbering: DocumentNumbering? = null,
    var theme: DocumentTheme? = null,
    val pageFormat: PageFormatInfo = PageFormatInfo(),
) {
    /**
     * The numbering formats of the document if set by the user,
     * otherwise the default numbering of the document [type] (which may also be `null`).
     * @see DocumentType.defaultNumbering
     */
    val numberingOrDefault: DocumentNumbering?
        get() = numbering ?: type.defaultNumbering
}
