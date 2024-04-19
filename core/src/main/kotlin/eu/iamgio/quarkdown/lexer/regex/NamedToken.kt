package eu.iamgio.quarkdown.lexer.regex

import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.walker.WalkerLexer
import eu.iamgio.quarkdown.visitor.token.TokenVisitor

/**
 * A particular type of [Token] returned by [WalkerLexer]s that interact with [RegexLexer]s.
 * When a [RegexLexer] encounters a [NamedToken], it will store its group in named groups
 * instead of regular groups.
 * @param name group name
 */
class NamedToken(val name: String, data: TokenData) : Token(data) {
    /**
     * @throws UnsupportedOperationException not a node-related token
     */
    override fun <T> accept(visitor: TokenVisitor<T>): T = throw UnsupportedOperationException()
}
