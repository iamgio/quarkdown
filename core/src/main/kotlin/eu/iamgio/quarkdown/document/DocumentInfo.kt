package eu.iamgio.quarkdown.document

import eu.iamgio.quarkdown.document.numbering.NumberingFormat
import eu.iamgio.quarkdown.document.page.PageFormatInfo
import eu.iamgio.quarkdown.localization.Locale

/**
 * Mutable information about the final artifact.
 * This data is mutated by library functions `.docname`, `.docauthor`, etc.
 * @param type type of the document
 * @param name name of the document, if specified
 * @param author author of the document, if specified
 * @param theme theme of the document, if specified
 * @param locale language of the document
 * @param numberingFormat format to
 * @param pageFormat format of the pages of the document
 */
data class DocumentInfo(
    var type: DocumentType = DocumentType.PLAIN,
    var name: String? = null,
    var author: String? = null,
    var locale: Locale? = null,
    var numberingFormat: NumberingFormat? = null,
    var theme: DocumentTheme? = null,
    val pageFormat: PageFormatInfo = PageFormatInfo(),
)
