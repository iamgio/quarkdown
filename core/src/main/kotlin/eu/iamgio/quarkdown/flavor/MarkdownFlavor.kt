package eu.iamgio.quarkdown.flavor

/**
 * A flavor of Markdown. Each flavor consists of its own rules and extensions.
 * @see eu.iamgio.quarkdown.flavor.base.BaseMarkdownFlavor
 * @see eu.iamgio.quarkdown.flavor.quarkdown.QuarkdownFlavor
 */
interface MarkdownFlavor {
    /**
     * The provider of new lexer instances to tokenize various scopes from a raw string input.
     */
    val lexerFactory: LexerFactory

    /**
     * The provider of new parser instances to obtain processed nodes from raw tokens.
     */
    val parserFactory: ParserFactory
}
