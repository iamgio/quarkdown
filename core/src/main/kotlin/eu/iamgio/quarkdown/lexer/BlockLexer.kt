package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.lexer.pattern.WhitespaceTokenRegexPattern

/**
 * A [Lexer] that tokenizes macro-blocks. A block contains further information that needs to be processed by other components.
 * @param source the content to be tokenized
 */
class BlockLexer(source: CharSequence) : StandardRegexLexer(
    source,
    patterns = WhitespaceTokenRegexPattern.values().toList(),
)
