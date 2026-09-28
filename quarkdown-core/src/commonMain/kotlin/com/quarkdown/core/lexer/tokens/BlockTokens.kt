package com.quarkdown.core.lexer.tokens

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.visitor.token.TokenVisitor

/**
 * A blank line.
 * @see com.quarkdown.core.ast.base.block.Newline
 */
class NewlineToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 *     Code
 * ```
 * @see com.quarkdown.core.ast.base.block.Code
 * @param content the code with up to four leading spaces removed from each line, untrimmed
 */
class BlockCodeToken(
    data: TokenData,
    val content: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ~~~
 * ```language
 * Code
 * ```
 * ~~~
 *
 * ```
 * ~~~language
 * Code
 * ~~~
 * ```
 * @see com.quarkdown.core.ast.base.block.Code
 * @param indent amount of spaces the opening fence is indented by
 * @param language raw language tag, if any
 * @param caption raw caption including its delimiters, if any
 * @param customId raw `{#custom-id}` identifier, if any
 * @param content raw fenced content, the fence lines excluded
 */
class FencesCodeToken(
    data: TokenData,
    val indent: Int,
    val language: String?,
    val caption: String?,
    val customId: String?,
    val content: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A multiline fenced block that contains a TeX expression.
 * This is a custom Quarkdown block.
 *
 * Example:
 * $$$
 * LaTeX expression line 1
 * LaTeX expression line 2
 * $$$
 * @see com.quarkdown.core.ast.quarkdown.block.Math
 * @param expression raw math expression, delimiters excluded
 * @param customId raw `{#custom-id}` identifier, if any
 */
class MultilineMathToken(
    data: TokenData,
    val expression: String,
    val customId: String?,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * An isolated one-line fenced block that contains a TeX expression.
 * If it's not isolated, then it's an [InlineMathToken].
 * This is a custom Quarkdown block.
 *
 * Example:
 * $ LaTeX expression $
 * @see com.quarkdown.core.ast.quarkdown.block.Math
 * @param expression raw math expression, delimiters excluded
 * @param customId raw `{#custom-id}` identifier, if any
 */
class OnelineMathToken(
    data: TokenData,
    val expression: String,
    val customId: String?,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A thematic break.
 * Examples:
 * ```
 * ---
 * ```
 * ```
 * *****
 * ```
 * @see com.quarkdown.core.ast.base.block.HorizontalRule
 */
class HorizontalRuleToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * A page break.
 * This is a custom Quarkdown block.
 *
 * Example:
 * ```
 * <<<
 * ```
 * @see com.quarkdown.core.ast.quarkdown.block.PageBreak
 */
class PageBreakToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * # Heading
 * ```
 * @param depth amount of `#` characters, from 1 to 6
 * @param isDecorative whether a `!` follows the hashes, marking the heading as not part of the structure
 * @param content raw heading text, delimiters and the trailing `#` run excluded
 * @param customId raw `{#custom-id}` identifier, if any
 * @see com.quarkdown.core.ast.base.block.Heading
 */
class HeadingToken(
    data: TokenData,
    val depth: Int,
    val isDecorative: Boolean,
    val content: String,
    val customId: String?,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * Heading
 * ====
 * ```
 * ```
 * Heading
 * ---
 * ```
 * @see com.quarkdown.core.ast.base.block.Heading
 * @param content raw heading text, spanning every line up to the underline
 * @param underline the run of `=` or `-` that underlines the heading
 * @param customId raw `{#custom-id}` identifier, if any
 */
class SetextHeadingToken(
    data: TokenData,
    val content: String,
    val underline: String,
    val customId: String?,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [label]: url "Title"
 * ```
 * @see com.quarkdown.core.ast.base.block.LinkDefinition
 * @param label raw reference label, brackets excluded
 * @param url raw destination, angle brackets included when it had them
 * @param title raw title including its delimiters, if any
 */
class LinkDefinitionToken(
    data: TokenData,
    val label: String,
    val url: String,
    val title: String?,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [^label]: Lorem ipsum
 *   dolor sit
 * amet.
 * ```
 * @see com.quarkdown.core.ast.base.block.FootnoteDefinition
 * @param label raw footnote label, the `[^` and `]` excluded
 * @param content raw definition text, untrimmed
 */
class FootnoteDefinitionToken(
    data: TokenData,
    val label: String,
    val content: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * - A
 * - B
 * ```
 * @see com.quarkdown.core.ast.base.block.list.UnorderedList
 */
class UnorderedListToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * 1. First
 * 2. Second
 * ```
 * @see com.quarkdown.core.ast.base.block.list.OrderedList
 * @param marker raw indentation and bullet of the first item, e.g. `1.`
 */
class OrderedListToken(
    data: TokenData,
    val marker: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * - A
 * ```
 * ```
 * 1. First
 * ```
 * @see com.quarkdown.core.ast.base.block.list.ListItem
 * @param marker raw indentation and bullet, e.g. `  -`
 * @param task raw GFM task marker including its leading separator, e.g. ` [x]`, or an empty string
 */
class ListItemToken(
    data: TokenData,
    val marker: String,
    val task: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * | foo | bar |
 * | --- | --- |
 * | baz | bim |
 * ```
 * ```
 * | foo  |  bar |
 * | :--- | ---: |
 * | baz  |  bim |
 * ```
 * @see com.quarkdown.core.ast.base.block.Table
 * @param header raw header row
 * @param alignment raw alignment row
 * @param rows raw cell rows, the metadata row included, or an empty string when the table has none
 */
class TableToken(
    data: TokenData,
    val header: String,
    val alignment: String,
    val rows: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <p>
 *     Content
 * </p>
 * ```
 * @see com.quarkdown.core.ast.base.block.Html
 */
class HtmlToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see com.quarkdown.core.ast.base.block.Paragraph
 */
class ParagraphToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * > Quote
 * ```
 * @see com.quarkdown.core.ast.base.block.BlockQuote
 * @param content the quote's text with the `>` markers of each line removed, untrimmed
 */
class BlockQuoteToken(
    data: TokenData,
    val content: String,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see com.quarkdown.core.ast.base.block.BlankNode
 */
class BlockTextToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}
