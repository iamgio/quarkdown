package eu.iamgio.quarkdown.document

import eu.iamgio.quarkdown.document.locale.Locale
import eu.iamgio.quarkdown.document.page.PageFormatInfo

/**
 * Mutable information about the final artifact.
 * This data is mutated by library functions `.docname`, `.docauthor`, etc.
 * @param type type of the document
 * @param name name of the document, if specified
 * @param author author of the document, if specified
 * @param theme theme of the document, if specified
 * @param locale language of the document
 * @param pageFormat format of the pages of the document
 */
data class DocumentInfo(
    var type: DocumentType = DocumentType.PLAIN,
    var name: String? = null,
    var author: String? = null,
    var locale: Locale? = null,
    var theme: DocumentTheme? = null,
    val pageFormat: PageFormatInfo = PageFormatInfo(),
)
