package eu.iamgio.quarkdown.lexer

import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.regex.pattern.BlockTokenRegexPattern

/**
 *
 */
class BlockLexer(source: CharSequence) : StandardRegexLexer(
    source,
    patterns = BlockTokenRegexPattern.values().toList(),
)
