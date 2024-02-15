package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.common.BlockTokenVisitor

/**
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class NewlineToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockCode
 */
class BlockCodeToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class FencesCodeToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.HorizontalRule
 */
class HorizontalRuleToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class HeadingToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class SetextHeadingToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.LinkDefinition
 */
class LinkDefinitionToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.ListItem
 */
class ListItemToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Html
 */
class HtmlToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Paragraph
 */
class ParagraphToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockQuote
 */
class BlockQuoteToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockText
 */
class BlockTextToken(token: Token) : TokenDecorator(token) {
    override fun <T> accept(visitor: BlockTokenVisitor<T>) = visitor.visit(this)
}
