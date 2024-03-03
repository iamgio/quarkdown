package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 * [BaseMarkdownFlavor] parser factory.
 * @param flavor the flavor instance to parse against
 */
class BaseMarkdownParserFactory(private val flavor: BaseMarkdownFlavor = BaseMarkdownFlavor()) : ParserFactory {
    override fun newBlockParser() = BlockTokenParser(flavor)
}
