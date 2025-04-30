package com.quarkdown.core.flavor.base

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.flavor.ParserFactory
import com.quarkdown.core.parser.BlockTokenParser
import com.quarkdown.core.parser.InlineTokenParser

/**
 * [BaseMarkdownFlavor] parser factory.
 */
class BaseMarkdownParserFactory : ParserFactory {
    override fun newBlockParser(context: MutableContext) = BlockTokenParser(context)

    override fun newInlineParser(context: MutableContext) = InlineTokenParser(context)
}
