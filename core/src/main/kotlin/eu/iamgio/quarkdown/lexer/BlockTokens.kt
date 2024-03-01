package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.ast.visitor.BlockTokenVisitor

/**
 * A blank line.
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class NewlineToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 *     Code
 * ```
 * @see eu.iamgio.quarkdown.ast.Code
 */
class BlockCodeToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
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
 * @see eu.iamgio.quarkdown.ast.Code
 */
class FencesCodeToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
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
 * @see eu.iamgio.quarkdown.ast.Math
 */
class MultilineMathToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * A one-line fenced block that contains a TeX expression.
 * This is a custom Quarkdown block.
 *
 * Example:
 * $ LaTeX expression $
 * @see eu.iamgio.quarkdown.ast.Math
 */
class OnelineMathToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * ---
 * ```
 * ```
 * *****
 * ```
 * @see eu.iamgio.quarkdown.ast.HorizontalRule
 */
class HorizontalRuleToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * # Heading
 * ```
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class HeadingToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
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
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class SetextHeadingToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * [label]: url "Title"
 * ```
 * @see eu.iamgio.quarkdown.ast.LinkDefinition
 */
class LinkDefinitionToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * - A
 * - B
 * ```
 * @see eu.iamgio.quarkdown.ast.UnorderedList
 */
class UnorderedListToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * 1. First
 * 2. Second
 * ```
 * @see eu.iamgio.quarkdown.ast.OrderedList
 */
class OrderedListToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * Examples:
 * ```
 * - A
 * ```
 * ```
 * 1. First
 * ```
 * @see eu.iamgio.quarkdown.ast.ListItem
 */
class ListItemToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * <p>
 *     Content
 * </p>
 * ```
 * @see eu.iamgio.quarkdown.ast.Html
 */
class HtmlToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Paragraph
 */
class ParagraphToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * Example:
 * ```
 * > Quote
 * ```
 * @see eu.iamgio.quarkdown.ast.BlockQuote
 */
class BlockQuoteToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockText
 */
class BlockTextToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}
