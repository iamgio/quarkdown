package com.quarkdown.core.flavor.base

import com.quarkdown.core.flavor.LexerFactory
import com.quarkdown.core.flavor.MarkdownFlavor
import com.quarkdown.core.flavor.ParserFactory
import com.quarkdown.core.flavor.RendererFactory
import com.quarkdown.core.flavor.TreeIteratorFactory

/**
 * The vanilla [CommonMark](https://spec.commonmark.org) Markdown with several [GFM](https://github.github.com/gfm) features and extensions.
 */
object BaseMarkdownFlavor : MarkdownFlavor {
    override val lexerFactory: LexerFactory = BaseMarkdownLexerFactory()
    override val parserFactory: ParserFactory = BaseMarkdownParserFactory()
    override val rendererFactory: RendererFactory = BaseMarkdownRendererFactory
    override val treeIteratorFactory: TreeIteratorFactory = BaseMarkdownTreeIteratorFactory()
}
