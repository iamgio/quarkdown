package eu.iamgio.quarkdown.ast.dsl

import eu.iamgio.quarkdown.ast.InlineContent
import eu.iamgio.quarkdown.ast.base.inline.CodeSpan
import eu.iamgio.quarkdown.ast.base.inline.Emphasis
import eu.iamgio.quarkdown.ast.base.inline.Image
import eu.iamgio.quarkdown.ast.base.inline.LineBreak
import eu.iamgio.quarkdown.ast.base.inline.Link
import eu.iamgio.quarkdown.ast.base.inline.Strong
import eu.iamgio.quarkdown.ast.base.inline.Text
import eu.iamgio.quarkdown.ast.quarkdown.inline.InlineCollapse

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
     * @see CodeSpan
     */
    fun codeSpan(text: String) = +CodeSpan(text)

    /**
     * @see Image
     */
    fun image(
        url: String,
        title: String? = null,
        width: Int? = null,
        height: Int? = null,
        label: InlineAstBuilder.() -> Unit,
    ) = +Image(Link(buildInline(label), url, title), width, height)

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
fun buildInline(block: InlineAstBuilder.() -> Unit): InlineContent {
    return InlineAstBuilder().apply(block).build()
}
