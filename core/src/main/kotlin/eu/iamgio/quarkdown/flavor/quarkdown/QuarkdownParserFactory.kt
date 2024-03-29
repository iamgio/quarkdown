package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.ast.context.MutableContext
import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.parser.BlockTokenParser
import eu.iamgio.quarkdown.parser.InlineTokenParser

/**
 * [QuarkdownFlavor] parser factory.
 */
class QuarkdownParserFactory() : ParserFactory {
    override fun newBlockParser(context: MutableContext) = BlockTokenParser(QuarkdownFlavor, context)

    override fun newInlineParser(context: MutableContext) = InlineTokenParser(QuarkdownFlavor, context)
}
