package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.common.BlockTokenVisitor

/**
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class NewlineToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockCode
 */
class BlockCodeToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class FencesCodeToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.HorizontalRule
 */
class HorizontalRuleToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class HeadingToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class SetextHeadingToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.LinkDefinition
 */
class LinkDefinitionToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.ListItem
 */
class ListItemToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Html
 */
class HtmlToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.Paragraph
 */
class ParagraphToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockQuote
 */
class BlockQuoteToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}

/**
 * @see eu.iamgio.quarkdown.ast.BlockText
 */
class BlockTextToken(token: Token) : TokenWrapper(token) {
    override fun <O> accept(visitor: BlockTokenVisitor<O>) = visitor.visit(this)
}
