package eu.iamgio.quarkdown.parser.visitor

/**
 * A visitor for [eu.iamgio.quarkdown.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface TokenVisitor<T> : BlockTokenVisitor<T> // , InlineTokenVisitor<O>
