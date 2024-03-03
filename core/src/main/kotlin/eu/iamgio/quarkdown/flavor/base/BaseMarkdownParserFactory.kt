package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 *
 */
class BaseMarkdownParserFactory(private val flavor: BaseMarkdownFlavor) : ParserFactory {
    override fun newBlockParser() = BlockTokenParser(flavor)
}
