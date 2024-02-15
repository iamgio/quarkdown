package eu.iamgio.quarkdown.common

/**
 * A visitor for [eu.iamgio.quarkdown.lexer.TokenWrapper]s.
 * @param O output type of the `visit` methods
 */
interface TokenVisitor<O> : BlockTokenVisitor<O> // , InlineTokenVisitor<O>
