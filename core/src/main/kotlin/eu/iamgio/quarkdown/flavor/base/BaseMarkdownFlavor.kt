package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.flavor.MarkdownFlavor

/**
 * The vanilla [CommonMark](https://spec.commonmark.org) Markdown with several [GFM](https://github.github.com/gfm) features and extensions.
 */
open class BaseMarkdownFlavor : MarkdownFlavor {
    override val lexerFactory: LexerFactory = BaseMarkdownLexerFactory()
}
