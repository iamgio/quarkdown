package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.ParserFactory

/**
 * The vanilla [CommonMark](https://spec.commonmark.org) Markdown with several [GFM](https://github.github.com/gfm) features and extensions.
 */
object BaseMarkdownFlavor : MarkdownFlavor {
    override val lexerFactory: LexerFactory = BaseMarkdownLexerFactory()
    override val parserFactory: ParserFactory = BaseMarkdownParserFactory()
}
