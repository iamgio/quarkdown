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
 */
class BlockCodeToken(
    data: TokenData,
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
 */
class FencesCodeToken(
    data: TokenData,
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
 */
class MultilineMathToken(
    data: TokenData,
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
 */
class OnelineMathToken(
    data: TokenData,
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
 * @see com.quarkdown.core.ast.base.block.Heading
 */
class HeadingToken(
    data: TokenData,
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
 */
class SetextHeadingToken(
    data: TokenData,
) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [label]: url "Title"
 * ```
 * @see com.quarkdown.core.ast.base.block.LinkDefinition
 */
class LinkDefinitionToken(
    data: TokenData,
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
 */
class FootnoteDefinitionToken(
    data: TokenData,
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
 */
class OrderedListToken(
    data: TokenData,
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
 */
class ListItemToken(
    data: TokenData,
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
 */
class TableToken(
    data: TokenData,
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
 */
class BlockQuoteToken(
    data: TokenData,
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
