package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.ParserFactory

/**
 * [eu.iamgio.quarkdown.flavor.base.BaseMarkdownFlavor] extension, with:
 * - Functions
 * - Math blocks
 * - To do...
 */
class QuarkdownFlavor : MarkdownFlavor {
    override val lexerFactory: LexerFactory = QuarkdownLexerFactory()
    override val parserFactory: ParserFactory = QuarkdownParserFactory(this)
}
