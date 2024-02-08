package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.lexer.pattern.WhitespaceTokenRegexPattern
import eu.iamgio.quarkdown.lexer.type.InlineTokenType

/**
 * A [Lexer] that tokenizes macro-blocks. A block contains further information that needs to be processed by other components.
 * @param source the content to be tokenized
 */
class BlockLexer(source: CharSequence) : RegexLexer(source, patterns = WhitespaceTokenRegexPattern.values().toList()) {
    // TODO use block patterns

    override fun createFillToken(position: IntRange): Token {
        val text = source.substring(position)

        // TODO literal value could also be number or boolean (need to parse here)
        // TODO use correct token type

        return Token(
            InlineTokenType.TEXT,
            text,
            text,
            position,
        )
    }

    override fun manipulate(tokens: List<Token>): List<Token> {
        return tokens // TODO filter?
    }
}
