package com.quarkdown.core.flavor.quarkdown

import com.quarkdown.core.flavor.LexerFactory
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.flavor.ParserFactory
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.flavor.TreeIteratorFactory

/**
 * [com.quarkdown.core.flavor.base.BaseMarkdownFlavor] extension with, in addition:
 * - Functions
 * - Math blocks
 * - Code span additional content
 * - Image labels
 * - Table of contents
 *
 * And more.
 */
object QuarkdownFlavor : MarkdownFlavor {
    override val lexerFactory: LexerFactory = QuarkdownLexerFactory
    override val parserFactory: ParserFactory = QuarkdownParserFactory()
    override val rendererFactory: RendererFactory = QuarkdownRendererFactory()
    override val treeIteratorFactory: TreeIteratorFactory = QuarkdownTreeIteratorFactory()
}
