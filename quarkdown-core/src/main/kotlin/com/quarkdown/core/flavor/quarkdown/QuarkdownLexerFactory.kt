package com.quarkdown.core.flavor.quarkdown

import com.quarkdown.core.flavor.LexerFactory
import com.quarkdown.core.flavor.base.BaseMarkdownLexerFactory
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.patterns.QuarkdownBlockTokenRegexPatterns
import com.quarkdown.core.lexer.patterns.QuarkdownInlineTokenRegexPatterns
import com.quarkdown.core.lexer.regex.StandardRegexLexer
import com.quarkdown.core.lexer.tokens.PlainTextToken

/**
 * [QuarkdownFlavor] lexer factory.
 */
object QuarkdownLexerFactory : LexerFactory {
    private val blockPatterns = QuarkdownBlockTokenRegexPatterns()
    private val inlinePatterns = QuarkdownInlineTokenRegexPatterns()
    private val base = BaseMarkdownLexerFactory

    /**
     * Inserts patterns of Quarkdown's inline extensions into the base inline lexer (produced by [BaseMarkdownLexerFactory]).
     * @return a copy of the base inline lexer also containing Quarkdown's inline extensions.
     */
    private fun StandardRegexLexer.insertInlineExtensions(): Lexer {
        // New inline patterns introduced by this flavor on top of the base patterns.
        val inlineExtensions =
            with(inlinePatterns) {
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
        with(blockPatterns) {
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
        with(inlinePatterns) {
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
