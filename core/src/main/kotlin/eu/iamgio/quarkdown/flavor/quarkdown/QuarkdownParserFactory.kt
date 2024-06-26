package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.base.BaseMarkdownParserFactory

/**
 * [QuarkdownFlavor] parser factory.
 * Quarkdown parsing doesn't differ from the base one, as new nodes are generated by the lexer,
 * instead of the parser.
 */
typealias QuarkdownParserFactory = BaseMarkdownParserFactory
