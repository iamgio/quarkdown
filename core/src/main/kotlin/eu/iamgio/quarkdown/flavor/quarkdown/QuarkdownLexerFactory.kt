package eu.iamgio.quarkdown.flavor.quarkdown

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.flavor.base.BaseMarkdownLexerFactory
import eu.iamgio.quarkdown.lexer.Lexer
import eu.iamgio.quarkdown.lexer.patterns.QuarkdownBlockTokenRegexPatterns
import eu.iamgio.quarkdown.lexer.patterns.QuarkdownInlineTokenRegexPatterns
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer
import eu.iamgio.quarkdown.lexer.tokens.PlainTextToken

/**
 * [QuarkdownFlavor] lexer factory.
 */
class QuarkdownLexerFactory : LexerFactory {
    private val base = BaseMarkdownLexerFactory()

    /**
     * New inline patterns introduced by this flavor.
     */
    private val inlineExtensions =
        with(QuarkdownInlineTokenRegexPatterns()) {
            listOf(
                inlineFunctionCall,
                inlineMath,
            )
        }

    override fun newBlockLexer(source: CharSequence): Lexer =
        with(QuarkdownBlockTokenRegexPatterns()) {
            StandardRegexLexer(
                source,
                listOf(
                    functionCall,
                    blockQuote,
                    blockCode,
                    linkDefinition,
                    fencesCode,
                    multilineMath,
                    onelineMath,
                    heading,
                    horizontalRule,
                    pageBreak,
                    setextHeading,
                    table,
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

    override fun newInlineLexer(source: CharSequence): Lexer =
        base.newInlineLexer(source).updatePatterns { patterns ->
            patterns + this.inlineExtensions
        }

    override fun newLinkLabelInlineLexer(source: CharSequence): Lexer =
        base.newLinkLabelInlineLexer(source).updatePatterns { patterns ->
            patterns + this.inlineExtensions
        }

    override fun newExpressionLexer(
        source: CharSequence,
        allowBlockFunctionCalls: Boolean,
    ): Lexer =
        with(QuarkdownInlineTokenRegexPatterns()) {
            // A function call argument contains textual content (string/number/...)
            // and possibly other nested function calls.
            StandardRegexLexer(
                source,
                if (allowBlockFunctionCalls) {
                    listOf(
                        escape,
                        QuarkdownBlockTokenRegexPatterns().functionCall,
                        inlineFunctionCall,
                    )
                } else {
                    listOf(
                        escape,
                        inlineFunctionCall,
                    )
                },
                fillTokenType = ::PlainTextToken,
            )
        }
}
