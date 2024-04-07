package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.parser.BlockTokenParser
import eu.iamgio.quarkdown.parser.InlineTokenParser

/**
 * [BaseMarkdownFlavor] parser factory.
 */
class BaseMarkdownParserFactory : ParserFactory {
    override fun newBlockParser(context: MutableContext) = BlockTokenParser(BaseMarkdownFlavor, context)

    override fun newInlineParser(context: MutableContext) = InlineTokenParser(BaseMarkdownFlavor, context)
}
