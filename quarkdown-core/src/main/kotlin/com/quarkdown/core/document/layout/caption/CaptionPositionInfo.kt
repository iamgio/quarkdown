package com.quarkdown.core.document.layout.caption

/**
 * Mutable information about the position of captions of [com.quarkdown.core.ast.quarkdown.CaptionableNode] nodes in a document.
 * @param default default relative position of captions
 * @param figures position of captions for [com.quarkdown.core.ast.quarkdown.block.Figure], if different from the default
 * @param tables position of captions for [com.quarkdown.core.ast.base.block.Table], if different from the default
 * @param codeBlocks position of captions for [com.quarkdown.core.ast.base.block.Code], if different from the default
 */
data class CaptionPositionInfo(
    var default: CaptionPosition = CaptionPosition.BOTTOM,
    var figures: CaptionPosition? = null,
    var tables: CaptionPosition? = null,
    var codeBlocks: CaptionPosition? = null,
) {
    /**
     * @return the value of [property] if it is not `null`, otherwise the [default] value.
     */
    fun getOrDefault(property: CaptionPositionInfo.() -> CaptionPosition?): CaptionPosition = property() ?: default
}
