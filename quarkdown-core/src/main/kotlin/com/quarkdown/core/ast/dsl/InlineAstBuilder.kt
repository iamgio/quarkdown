package com.quarkdown.core.ast.dsl

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.LineBreak
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.inline.InlineCollapse
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData
import com.quarkdown.core.document.size.Size

/**
 * A builder of inline nodes.
 */
class InlineAstBuilder : AstBuilder() {
    /**
     * @see Strong
     */
    fun strong(block: InlineAstBuilder.() -> Unit) = +Strong(buildInline(block))

    /**
     * @see Emphasis
     */
    fun emphasis(block: InlineAstBuilder.() -> Unit) = +Emphasis(buildInline(block))

    /**
     * @see Text
     */
    fun text(text: String) = +Text(text)

    /**
     * @see TextTransform
     */
    fun text(
        text: String,
        transform: TextTransformData,
    ) = +TextTransform(transform, children = buildInline { text(text) })

    /**
     * @see Link
     */
    fun link(
        url: String,
        title: String? = null,
        label: InlineAstBuilder.() -> Unit,
    ) = +Link(buildInline(label), url, title)

    /**
     * @see CodeSpan
     */
    fun codeSpan(text: String) = +CodeSpan(text)

    /**
     * @see Image
     */
    fun image(
        url: String,
        title: String? = null,
        width: Size? = null,
        height: Size? = null,
        referenceId: String? = null,
        label: InlineAstBuilder.() -> Unit = {},
    ) = +Image(Link(buildInline(label), url, title), width, height, referenceId)

    /**
     * @see InlineCollapse
     */
    fun collapse(
        text: InlineAstBuilder.() -> Unit,
        placeholder: InlineAstBuilder.() -> Unit = { text(InlineCollapse.DEFAULT_PLACEHOLDER) },
        isOpen: Boolean = false,
    ) = +InlineCollapse(buildInline(text), buildInline(placeholder), isOpen)

    /**
     * Automatically collapses a text if its length exceeds [maxLength].
     * @see InlineCollapse
     */
    fun autoCollapse(
        text: String,
        maxLength: Int,
    ) = collapse(
        text = { text(text) },
        isOpen = text.length <= maxLength,
    )

    /**
     * @see LineBreak
     */
    fun lineBreak() = +LineBreak
}

/**
 * Begins a DSL block for building inline content.
 * @param block action to run with the inline builder
 * @return the built nodes
 * @see InlineAstBuilder
 */
fun buildInline(block: InlineAstBuilder.() -> Unit): InlineContent = InlineAstBuilder().apply(block).build()
