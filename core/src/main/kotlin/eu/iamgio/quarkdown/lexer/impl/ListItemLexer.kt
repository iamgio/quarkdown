package eu.iamgio.quarkdown.lexer.impl

import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.regex.pattern.BlockTokenRegexPattern

/**
 * A [Lexer] that extracts list items from a list.
 * Along with list items, blank lines are extracted too, since they determine whether a list is loose or not.
 * @param source the text content of the list block
 */
class ListItemLexer(source: CharSequence) : StandardRegexLexer(
    source,
    patterns = listOf(BlockTokenRegexPattern.LISTITEM, BlockTokenRegexPattern.NEWLINE),
)
