package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.common.BlockTokenVisitor

/**
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class NewlineToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockCode
 */
class BlockCodeToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Code
 */
class FencesCodeToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.HorizontalRule
 */
class HorizontalRuleToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class HeadingToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class SetextHeadingToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.LinkDefinition
 */
class LinkDefinitionToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.ListItem
 */
class ListItemToken(token: TokenData) : Token(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
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
