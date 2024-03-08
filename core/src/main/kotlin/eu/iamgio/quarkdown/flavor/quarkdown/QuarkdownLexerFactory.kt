package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.flavor.base.BaseMarkdownLexerFactory
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer

/**
 * [QuarkdownFlavor] lexer factory.
 */
class QuarkdownLexerFactory : LexerFactory {
    private val base = BaseMarkdownLexerFactory()

    override fun newBlockLexer(source: CharSequence): Lexer =
        with(QuarkdownBlockTokenRegexPatterns()) {
            StandardRegexLexer(
                source,
                listOf(
                    blockQuote,
                    blockCode,
                    linkDefinition,
                    fencesCode,
                    multilineMath,
                    onelineMath,
                    heading,
                    horizontalRule,
                    setextHeading,
                    html,
                    unorderedList,
                    orderedList,
                    newline,
                    paragraph,
                    blockText,
                ),
            )
        }

    override fun newListLexer(source: CharSequence): Lexer = base.newListLexer(source)

    override fun newInlineLexer(source: CharSequence) = base.newInlineLexer(source)

    override fun newInlineEmphasisLexer(source: CharSequence) = base.newInlineEmphasisLexer(source)
}
