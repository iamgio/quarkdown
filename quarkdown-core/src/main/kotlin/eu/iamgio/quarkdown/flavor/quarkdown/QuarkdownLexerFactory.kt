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
     * Inserts patterns of Quarkdown's inline extensions into the base inline lexer (produced by [BaseMarkdownLexerFactory]).
     * @return a copy of the base inline lexer also containing Quarkdown's inline extensions.
     */
    private fun StandardRegexLexer.insertInlineExtensions(): Lexer {
        // New inline patterns introduced by this flavor on top of the base patterns.
        val inlineExtensions =
            with(QuarkdownInlineTokenRegexPatterns()) {
                listOf(
                    inlineFunctionCall,
                    inlineMath,
                    *textReplacements.toTypedArray(),
                )
            }

        // The last pattern is the critical content one, which should always last.
        return this.updatePatterns { patterns ->
            patterns.dropLast(1) + inlineExtensions + patterns.last()
        }
    }

    override fun newBlockLexer(source: CharSequence): Lexer =
        with(QuarkdownBlockTokenRegexPatterns()) {
            StandardRegexLexer(
                source,
                listOf(
                    comment,
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
                    unorderedList,
                    orderedList,
                    newline,
                    paragraph,
                    blockText,
                ),
            )
        }

    override fun newListLexer(source: CharSequence): Lexer = base.newListLexer(source)

    override fun newInlineLexer(source: CharSequence): Lexer = base.newInlineLexer(source).insertInlineExtensions()

    override fun newLinkLabelInlineLexer(source: CharSequence): Lexer = base.newLinkLabelInlineLexer(source).insertInlineExtensions()

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
