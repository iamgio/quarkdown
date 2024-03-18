package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.ast.MutableAstAttributes
import eu.iamgio.quarkdown.flavor.ParserFactory
import eu.iamgio.quarkdown.parser.BlockTokenParser
import eu.iamgio.quarkdown.parser.InlineTokenParser

/**
 * [BaseMarkdownFlavor] parser factory.
 */
class BaseMarkdownParserFactory : ParserFactory {
    override fun newBlockParser(attributes: MutableAstAttributes) = BlockTokenParser(BaseMarkdownFlavor, attributes)

    override fun newInlineParser(attributes: MutableAstAttributes) = InlineTokenParser(BaseMarkdownFlavor, attributes)
}
