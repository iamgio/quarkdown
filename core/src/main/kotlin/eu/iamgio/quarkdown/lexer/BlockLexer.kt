package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.regex.pattern.BlockTokenRegexPattern

/**
 * A [Lexer] that splits the source into macro-blocks tokens.
 * @param source the content to be tokenized
 */
class BlockLexer(source: CharSequence) : StandardRegexLexer(
    source,
    patterns = BlockTokenRegexPattern.values().toList(),
)
