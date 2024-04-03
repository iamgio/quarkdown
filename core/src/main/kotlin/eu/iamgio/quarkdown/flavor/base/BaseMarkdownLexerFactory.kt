package eu.iamgio.quarkdown.flavor.base

import eu.iamgio.quarkdown.flavor.LexerFactory
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.lexer.patterns.BaseMarkdownBlockTokenRegexPatterns
import eu.iamgio.quarkdown.lexer.patterns.BaseMarkdownInlineTokenRegexPatterns
import eu.iamgio.quarkdown.lexer.regex.StandardRegexLexer

/**
 * [BaseMarkdownFlavor] lexer factory.
 */
class BaseMarkdownLexerFactory : LexerFactory {
    override fun newBlockLexer(source: CharSequence): StandardRegexLexer =
        with(BaseMarkdownBlockTokenRegexPatterns()) {
            StandardRegexLexer(
                source,
                listOf(
                    blockQuote,
                    blockCode,
                    linkDefinition,
                    fencesCode,
                    heading,
                    horizontalRule,
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
}
