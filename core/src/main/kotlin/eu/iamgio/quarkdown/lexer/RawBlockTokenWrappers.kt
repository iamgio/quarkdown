package eu.iamgio.quarkdown.lexer

/**
 * A wrapper of a [Token] that may be parsed by a specific parser in order to extract information.
 * @param token the wrapped token
 */
sealed class RawTokenWrapper(val token: RawToken)

/**
 * @see eu.iamgio.quarkdown.lexer.type.Newline
 */
class RawNewline(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.BlockCode
 */
class RawBlockCode(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.Newline
 */
class RawFencesCode(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.HorizontalRule
 */
class RawHorizontalLine(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.Heading
 */
class RawHeading(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.Heading
 */
class RawSetextHeading(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.LinkDefinition
 */
class RawLinkDefinition(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.ListItem
 */
class RawListItem(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.Html
 */
class RawHtml(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.Paragraph
 */
class RawParagraph(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.BlockQuote
 */
class RawBlockQuote(token: RawToken) : RawTokenWrapper(token)

/**
 * @see eu.iamgio.quarkdown.lexer.type.BlockText
 */
class RawBlockText(token: RawToken) : RawTokenWrapper(token)
