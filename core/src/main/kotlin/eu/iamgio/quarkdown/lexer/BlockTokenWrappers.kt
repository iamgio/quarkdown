package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.ast.Node
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 * A wrapper of a [Token] that may be parsed by a specific parser in order to extract information.
 * @param data the wrapped token
 */
sealed class TokenWrapper(val data: Token) {
    abstract fun parse(parser: BlockTokenParser): Node
}

/**
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class NewlineToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockCode
 */
class BlockCodeToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class FencesCodeToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.HorizontalRule
 */
class HorizontalRuleToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class HeadingToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class SetextHeadingToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.LinkDefinition
 */
class LinkDefinitionToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.ListItem
 */
class ListItemToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Html
 */
class HtmlToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Paragraph
 */
class ParagraphToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockQuote
 */
class BlockQuoteToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockText
 */
class BlockTextToken(token: Token) : TokenWrapper(token) {
    override fun parse(parser: BlockTokenParser) = parser.visit(this)
}
