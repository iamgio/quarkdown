package com.quarkdown.core.document

import com.quarkdown.amber.annotations.NestedData
import com.quarkdown.core.document.layout.DocumentLayoutInfo
import com.quarkdown.core.document.numbering.DocumentNumbering
import com.quarkdown.core.document.tex.TexInfo
import com.quarkdown.core.localization.Locale

/**
 * Immutable information about the document.
 * This data is updated by library functions `.docname`, `.docauthor`, etc., by overwriting [com.quarkdown.core.context.MutableContext.documentInfo].
 * @param type type of the document
 * @param name name of the document, if specified
 * @param authors authors of the document, if specified
 * @param theme theme of the document, if specified
 * @param locale language of the document
 * @param numbering formats to apply to element numbering across the document
 * @param pageFormat format of the pages of the document
 */
@NestedData
data class DocumentInfo(
    val type: DocumentType = DocumentType.PLAIN,
    val name: String? = null,
    val authors: List<DocumentAuthor> = mutableListOf(),
    val locale: Locale? = null,
    val numbering: DocumentNumbering? = null,
    val theme: DocumentTheme? = null,
    val tex: TexInfo = TexInfo(),
    val layout: DocumentLayoutInfo = DocumentLayoutInfo(),
) {
    /**
     * The numbering formats of the document if set by the user,
     * otherwise the default numbering of the document [type] (which may also be `null`).
     * @see DocumentType.defaultNumbering
     */
    val numberingOrDefault: DocumentNumbering?
        get() = numbering ?: type.defaultNumbering
}
