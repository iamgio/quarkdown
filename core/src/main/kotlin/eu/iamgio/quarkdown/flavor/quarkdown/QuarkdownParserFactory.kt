package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 *
 */
class QuarkdownParserFactory(private val flavor: QuarkdownFlavor) : ParserFactory {
    override fun newBlockParser() = BlockTokenParser(flavor)
}
