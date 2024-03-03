package eu.iamgio.quarkdown.lexer.impl

import eu.iamgio.quarkdown.flavor.base._BaseBlockTokenRegexPattern
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer

/**
 * A [Lexer] that splits the source into macro-blocks tokens.
 * @param source the content to be tokenized
 */
class BlockLexer(source: CharSequence) : StandardRegexLexer(
    source,
    patterns = _BaseBlockTokenRegexPattern.values().toList(),
)
