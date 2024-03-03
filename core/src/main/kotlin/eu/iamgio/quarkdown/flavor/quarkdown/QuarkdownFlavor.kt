package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.ParserFactory

/**
 *
 */
class QuarkdownFlavor : MarkdownFlavor {
    override val lexerFactory: LexerFactory = QuarkdownLexerFactory()
    override val parserFactory: ParserFactory = QuarkdownParserFactory(this)
}
