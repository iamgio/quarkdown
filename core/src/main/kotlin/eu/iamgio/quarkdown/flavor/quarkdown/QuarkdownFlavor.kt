package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.flavor.RendererFactory

/**
 * [eu.iamgio.quarkdown.flavor.base.BaseMarkdownFlavor] extension, with:
 * - Functions
 * - Math blocks
 * - To do...
 */
object QuarkdownFlavor : MarkdownFlavor {
    override val lexerFactory: LexerFactory = QuarkdownLexerFactory()
    override val parserFactory: ParserFactory = QuarkdownParserFactory()
    override val rendererFactory: RendererFactory = QuarkdownRendererFactory()
}
