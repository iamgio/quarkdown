package eu.iamgio.quarkdown.lexer

/**
 * A wrapper of a [Token] that may be parsed by a specific parser in order to extract information.
 * @param token the wrapped token
 */
sealed class TokenWrapper(val token: Token)

/**
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class NewlineToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.BlockCode
 */
class BlockCodeToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.Newline
 */
class FencesCodeToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.HorizontalRule
 */
class HorizontalRuleToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class HeadingToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.Heading
 */
class SetextHeadingToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.LinkDefinition
 */
class LinkDefinitionToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.ListItem
 */
class ListItemToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.Html
 */
class HtmlToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.Paragraph
 */
class ParagraphToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.BlockQuote
 */
class BlockQuoteToken(token: Token) : TokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.ast.BlockText
 */
class BlockTextToken(token: Token) : TokenWrapper(token)
