package eu.iamgio.quarkdown.lexer.tokens

import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.visitor.token.TokenVisitor

/**
 * A blank line.
 * @see eu.iamgio.quarkdown.ast.base.block.Newline
 */
class NewlineToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 *     Code
 * ```
 * @see eu.iamgio.quarkdown.ast.base.block.Code
 */
class BlockCodeToken(data: TokenData) : Token(data) {
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
 * @see eu.iamgio.quarkdown.ast.base.block.Code
 */
class FencesCodeToken(data: TokenData) : Token(data) {
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
 * @see eu.iamgio.quarkdown.ast.quarkdown.block.Math
 */
class MultilineMathToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * An isolated one-line fenced block that contains a TeX expression.
 * If it's not isolated, then it's an [InlineMathToken].
 * This is a custom Quarkdown block.
 *
 * Example:
 * $ LaTeX expression $
 * @see eu.iamgio.quarkdown.ast.quarkdown.block.Math
 */
class OnelineMathToken(data: TokenData) : Token(data) {
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
 * @see eu.iamgio.quarkdown.ast.base.block.HorizontalRule
 */
class HorizontalRuleToken(data: TokenData) : Token(data) {
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
 * @see eu.iamgio.quarkdown.ast.quarkdown.block.PageBreak
 */
class PageBreakToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * # Heading
 * ```
 * @see eu.iamgio.quarkdown.ast.base.block.Heading
 */
class HeadingToken(data: TokenData) : Token(data) {
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
 * @see eu.iamgio.quarkdown.ast.base.block.Heading
 */
class SetextHeadingToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [label]: url "Title"
 * ```
 * @see eu.iamgio.quarkdown.ast.base.block.LinkDefinition
 */
class LinkDefinitionToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * - A
 * - B
 * ```
 * @see eu.iamgio.quarkdown.ast.base.block.UnorderedList
 */
class UnorderedListToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * 1. First
 * 2. Second
 * ```
 * @see eu.iamgio.quarkdown.ast.base.block.OrderedList
 */
class OrderedListToken(data: TokenData) : Token(data) {
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
 * @see eu.iamgio.quarkdown.ast.base.block.ListItem
 */
class ListItemToken(data: TokenData) : Token(data) {
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
 * @see eu.iamgio.quarkdown.ast.base.block.Table
 */
class TableToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <p>
 *     Content
 * </p>
 * ```
 * @see eu.iamgio.quarkdown.ast.base.block.Html
 */
class HtmlToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.base.block.Paragraph
 */
class ParagraphToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * > Quote
 * ```
 * @see eu.iamgio.quarkdown.ast.base.block.BlockQuote
 */
class BlockQuoteToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.base.block.BlockText
 */
class BlockTextToken(data: TokenData) : Token(data) {
    override fun <T> accept(visitor: TokenVisitor<T>) = visitor.visit(this)
}
