package com.quarkdown.core.flavor.base

import com.quarkdown.core.flavor.LexerFactory
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.patterns.BaseMarkdownBlockTokenRegexPatterns
import com.quarkdown.core.lexer.patterns.BaseMarkdownInlineTokenRegexPatterns
import com.quarkdown.core.lexer.regex.StandardRegexLexer
import com.quarkdown.core.lexer.tokens.PlainTextToken

/**
 * [BaseMarkdownFlavor] lexer factory.
 */
class BaseMarkdownLexerFactory : LexerFactory {
    override fun newBlockLexer(source: CharSequence): StandardRegexLexer =
        with(BaseMarkdownBlockTokenRegexPatterns()) {
            StandardRegexLexer(
                source,
                listOf(
                    comment,
                    blockQuote,
                    blockCode,
                    footnoteDefinition,
                    linkDefinition,
                    fencesCode,
                    heading,
                    horizontalRule,
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

    override fun newListLexer(source: CharSequence): StandardRegexLexer =
        with(BaseMarkdownBlockTokenRegexPatterns()) {
            StandardRegexLexer(
                source,
                listOf(listItem, newline),
            )
        }

    override fun newInlineLexer(source: CharSequence): StandardRegexLexer =
        newLinkLabelInlineLexer(source).updatePatterns { patterns ->
            with(BaseMarkdownInlineTokenRegexPatterns()) {
                listOf(
                    diamondAutolink,
                    link,
                    referenceLink,
                    urlAutolink,
                ) + patterns
            }
        }

    override fun newLinkLabelInlineLexer(source: CharSequence): StandardRegexLexer =
        with(BaseMarkdownInlineTokenRegexPatterns()) {
            StandardRegexLexer(
                source,
                listOf(
                    lineBreak,
                    codeSpan,
                    escape,
                    entity,
                    comment,
                    image,
                    referenceImage,
                    strongEmphasisAsterisk,
                    strongEmphasisUnderscore,
                    emphasisAsterisk,
                    emphasisUnderscore,
                    strongAsterisk,
                    strongUnderscore,
                    strikethrough,
                    criticalContent,
                ),
                fillTokenType = ::PlainTextToken,
            )
        }

    // Functions aren't supported by this flavor
    override fun newExpressionLexer(
        source: CharSequence,
        allowBlockFunctionCalls: Boolean,
    ): Lexer = StandardRegexLexer(source, patterns = emptyList())
}
