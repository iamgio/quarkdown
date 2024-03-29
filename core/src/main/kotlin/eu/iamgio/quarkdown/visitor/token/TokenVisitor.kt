package eu.iamgio.quarkdown.visitor.token

/**
 * A visitor for [eu.iamgio.quarkdown.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface TokenVisitor<T> : BlockTokenVisitor<T>, InlineTokenVisitor<T>
