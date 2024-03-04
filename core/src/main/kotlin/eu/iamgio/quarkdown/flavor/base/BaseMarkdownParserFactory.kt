package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 * [BaseMarkdownFlavor] parser factory.
 */
class BaseMarkdownParserFactory : ParserFactory {
    override fun newBlockParser() = BlockTokenParser(BaseMarkdownFlavor)
}
