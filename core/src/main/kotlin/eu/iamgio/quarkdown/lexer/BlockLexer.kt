package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.regex.pattern.BlockTokenRegexPattern
import eu.iamgio.quarkdown.lexer.regex.pattern.WhitespaceTokenRegexPattern
import eu.iamgio.quarkdown.lexer.type.InlineTokenType

/**
 * A [Lexer] that tokenizes macro-blocks. A block contains further information that needs to be processed by other components.
 * @param source the content to be tokenized
 */
class BlockLexer(source: CharSequence) : StandardRegexLexer(
    source,
    patterns = BlockTokenRegexPattern.values().toList() + WhitespaceTokenRegexPattern.EOL,
    fillTokenType = InlineTokenType.TEXT,
)
