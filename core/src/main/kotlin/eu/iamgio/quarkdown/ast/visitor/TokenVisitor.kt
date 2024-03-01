package eu.iamgio.quarkdown.ast.visitor

/**
 * A visitor for [eu.iamgio.quarkdown.lexer.Token]s.
 * @param T output type of the `visit` methods
 */
interface TokenVisitor<T> : BlockTokenVisitor<T> // , InlineTokenVisitor<O>
