package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.attributes.localization.LocalizedKind
import com.quarkdown.core.ast.attributes.localization.LocalizedKindKeys
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.quarkdown.CaptionableNode
import com.quarkdown.core.function.value.data.Range
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A code block.
 * @param content code content
 * @param language optional syntax language
 * @param showLineNumbers whether to show line numbers
 * @param focusedLines range of lines to focus on. No lines are focused if `null`
 */
class Code(
    val content: String,
    val language: String?,
    val showLineNumbers: Boolean = true,
    val focusedLines: Range? = null,
    override val caption: String? = null,
) : LocationTrackableNode,
    CaptionableNode,
    LocalizedKind {
    override val kindLocalizationKey: String
        get() = LocalizedKindKeys.CODE_BLOCK

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
