package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.flavor.MarkdownFlavor
import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.flavor.RendererFactory
import eu.iamgio.quarkdown.flavor.TreeIteratorFactory

/**
 * [eu.iamgio.quarkdown.flavor.base.BaseMarkdownFlavor] extension with, in addition:
 * - Functions
 * - Math blocks
 * - Code span additional content
 * - Image labels
 * - Table of contents
 *
 * And more.
 */
object QuarkdownFlavor : MarkdownFlavor {
    override val lexerFactory: LexerFactory = QuarkdownLexerFactory()
    override val parserFactory: ParserFactory = QuarkdownParserFactory()
    override val rendererFactory: RendererFactory = QuarkdownRendererFactory()
    override val treeIteratorFactory: TreeIteratorFactory = QuarkdownTreeIteratorFactory()
}
