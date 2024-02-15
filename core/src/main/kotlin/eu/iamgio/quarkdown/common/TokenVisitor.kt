package eu.iamgio.quarkdown.common

/**
 * A visitor for [eu.iamgio.quarkdown.lexer.TokenWrapper]s.
 * @param T output type of the `visit` methods
 */
interface TokenVisitor<T> : BlockTokenVisitor<T> // , InlineTokenVisitor<O>
