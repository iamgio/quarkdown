package com.quarkdown.core.ast.base.block

import com.quarkdown.core.ast.attributes.localization.LocalizedKind
import com.quarkdown.core.ast.attributes.localization.LocalizedKindKeys
import com.quarkdown.core.ast.attributes.location.LocationTrackableNode
import com.quarkdown.core.ast.quarkdown.CaptionableNode
import com.quarkdown.core.ast.quarkdown.reference.CrossReferenceableNode
import com.quarkdown.core.function.value.data.Range
import com.quarkdown.core.visitor.node.NodeVisitor

/**
 * A code block.
 * @param content code content
 * @param language optional syntax language
 * @param showLineNumbers whether to show line numbers
 * @param highlight whether to apply syntax highlighting
 * @param focusedLines range of lines to focus on. No lines are focused if `null`
 * @param caption optional caption
 * @param referenceId optional ID for cross-referencing via a [com.quarkdown.core.ast.quarkdown.reference.CrossReference]
 */
class Code(
    val content: String,
    val language: String?,
    val showLineNumbers: Boolean = true,
    val highlight: Boolean = true,
    val focusedLines: Range? = null,
    override val caption: String? = null,
    override val referenceId: String? = null,
) : LocationTrackableNode,
    CrossReferenceableNode,
    CaptionableNode,
    LocalizedKind {
    override val kindLocalizationKey: String
        get() = LocalizedKindKeys.CODE_BLOCK

    override fun <T> accept(visitor: NodeVisitor<T>) = visitor.visit(this)
}
