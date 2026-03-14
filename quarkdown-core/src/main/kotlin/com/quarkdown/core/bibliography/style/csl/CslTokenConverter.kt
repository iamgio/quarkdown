package com.quarkdown.core.bibliography.style.csl

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData
import de.undercouch.citeproc.csl.internal.TokenBuffer
import de.undercouch.citeproc.csl.internal.behavior.FormattingAttributes
import de.undercouch.citeproc.csl.internal.token.DisplayGroupToken
import de.undercouch.citeproc.csl.internal.token.TextToken

/**
 * Converts citeproc-java [TokenBuffer] tokens into Quarkdown [InlineContent] AST nodes.
 *
 * This is the bridge between citeproc-java's internal token representation and Quarkdown's AST,
 * mapping formatting attributes (italic, bold, small caps) and token types (URL, DOI) to
 * their corresponding Quarkdown node types.
 *
 * @param urlFormatter resolves raw URL/DOI text into display URLs
 *                     (e.g. prepending `https://doi.org/` to DOIs)
 */
internal class CslTokenConverter(
    private val urlFormatter: UrlFormatter,
) {
    /**
     * Resolves raw URL/DOI text into display URLs.
     */
    fun interface UrlFormatter {
        /**
         * @param text the raw URL or DOI text
         * @param type the token type (URL or DOI)
         * @return the formatted URL string
         */
        fun format(
            text: String,
            type: TextToken.Type,
        ): String
    }

    /**
     * Converts all tokens in a [TokenBuffer] into Quarkdown [InlineContent] AST nodes.
     *
     * [TextToken]s are converted based on their type and formatting attributes.
     * [DisplayGroupToken]s are skipped, as Quarkdown handles layout via CSS.
     */
    fun convert(buffer: TokenBuffer): InlineContent =
        buildList {
            for (token in buffer.tokens) {
                when (token) {
                    is TextToken -> if (token.text.isNotEmpty()) add(convertTextToken(token))
                    is DisplayGroupToken -> continue
                }
            }
        }

    /**
     * Extracts plain text from a [TokenBuffer], discarding formatting.
     */
    fun extractPlainText(buffer: TokenBuffer): String =
        buffer.tokens
            .filterIsInstance<TextToken>()
            .joinToString("") { it.text }
            .trim()

    /**
     * Converts a single [TextToken] into a Quarkdown AST node.
     *
     * URL/DOI tokens produce [Link] nodes. Other tokens produce [Text] nodes,
     * optionally wrapped in [Emphasis], [Strong], or [TextTransform]
     * based on their [FormattingAttributes].
     */
    private fun convertTextToken(token: TextToken): Node {
        if (token.type == TextToken.Type.URL || token.type == TextToken.Type.DOI) {
            return convertLinkToken(token.type, token.text)
        }

        return applyFormatting(Text(token.text), token.formattingAttributes)
    }

    /**
     * Converts a URL or DOI token into a [Link] node.
     */
    private fun convertLinkToken(
        type: TextToken.Type,
        text: String,
    ): Link {
        val url = urlFormatter.format(text, type)
        return Link(
            label = listOf(Text(url)),
            url = url,
            title = null,
        )
    }

    /**
     * Wraps a [node] in formatting nodes based on the given [FormattingAttributes] bitmask.
     * Attributes are applied innermost to outermost: italic, then bold, then small caps.
     */
    private fun applyFormatting(
        node: Node,
        attrs: Int,
    ): Node {
        var result = node

        if (FormattingAttributes.getFontStyle(attrs) == FormattingAttributes.FS_ITALIC) {
            result = Emphasis(text = listOf(result))
        }

        if (FormattingAttributes.getFontWeight(attrs) == FormattingAttributes.FW_BOLD) {
            result = Strong(text = listOf(result))
        }

        if (FormattingAttributes.getFontVariant(attrs) == FormattingAttributes.FV_SMALLCAPS) {
            result =
                TextTransform(
                    data = TextTransformData(variant = TextTransformData.Variant.SMALL_CAPS),
                    children = listOf(result),
                )
        }

        return result
    }
}
