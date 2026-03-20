package com.quarkdown.core.document.layout

import com.quarkdown.core.document.layout.caption.CaptionPositionInfo
import com.quarkdown.core.document.layout.font.FontInfo
import com.quarkdown.core.document.layout.page.PageFormatInfo
import com.quarkdown.core.document.layout.page.mergeAll
import com.quarkdown.core.document.layout.paragraph.ParagraphStyleInfo

/**
 * Mutable information about the layout options of a document.
 * When any of the fields is `null`, the default value supplied by the underlying renderer is used.
 * @param pageFormats format of the pages of the document, in ascending order of precedence
 * @param fonts list of font configurations used in the document, in order of precedence
 * @param paragraphStyle style of paragraphs in the document
 * @param captionPosition position of captions of figures, tables, and more in the document
 */
data class DocumentLayoutInfo(
    val pageFormats: List<PageFormatInfo> = emptyList(),
    val fonts: List<FontInfo> = emptyList(),
    val paragraphStyle: ParagraphStyleInfo = ParagraphStyleInfo(),
    val captionPosition: CaptionPositionInfo = CaptionPositionInfo(),
) {
    /**
     * Returns the effective page formats by prepending [default] (if non-null)
     * and folding formats that share the same [PageFormatInfo.selector].
     *
     * Within each selector group, later formats take priority: their non-null fields
     * override earlier ones via `merge`, while null fields inherit from the layer beneath.
     */
    fun getPageFormatsWithDefault(default: PageFormatInfo?): List<PageFormatInfo> {
        val all = default?.let { listOf(it) + pageFormats } ?: pageFormats

        return all
            .groupBy { it.selector }
            .values
            .map { formats -> formats.mergeAll() }
    }
}
