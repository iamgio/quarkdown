package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.parser.BlockTokenParser
import eu.iamgio.quarkdown.parser.InlineTokenParser

/**
 * [QuarkdownFlavor] parser factory.
 */
class QuarkdownParserFactory() : ParserFactory {
    override fun newBlockParser(attributes: MutableAstAttributes) = BlockTokenParser(QuarkdownFlavor, attributes)

    override fun newInlineParser(attributes: MutableAstAttributes) = InlineTokenParser(QuarkdownFlavor, attributes)
}
