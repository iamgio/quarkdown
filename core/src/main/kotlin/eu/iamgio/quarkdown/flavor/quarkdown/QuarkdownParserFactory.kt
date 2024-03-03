package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 * [QuarkdownFlavor] parser factory.
 * @param flavor the flavor instance to parse against (TODO use object for flavors instead of class to avoid this!)
 */
class QuarkdownParserFactory(private val flavor: QuarkdownFlavor) : ParserFactory {
    override fun newBlockParser() = BlockTokenParser(flavor)
}
