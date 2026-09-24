package com.quarkdown.core.bibliography.style.csl

import com.quarkdown.bibliographer.token.BibliographyToken
import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.inline.TextTransform
import com.quarkdown.core.ast.quarkdown.inline.TextTransformData

/**
 * Converts [kotlin-bibliographer](https://github.com/quarkdown-labs/kotlin-bibliographer)
 * [BibliographyToken]s into Quarkdown [InlineContent] AST nodes.
 *
 * This is the bridge between the library's platform-agnostic token domain and Quarkdown's AST:
 * formatting decorators (italic, bold, small caps, ...) recursively map
 * to their corresponding Quarkdown node types.
 */
internal object CslTokenConverter {
    /**
     * Converts a list of tokens into Quarkdown [InlineContent] AST nodes.
     */
    fun convert(tokens: List<BibliographyToken>): InlineContent = tokens.map(::convert)

    /**
     * Converts a single token into a Quarkdown AST node,
     * recursively unwrapping formatting decorators.
     */
    private fun convert(token: BibliographyToken): Node =
        when (token) {
            is BibliographyToken.Text -> Text(token.text)

            is BibliographyToken.Link ->
                Link(
                    label = listOf(convert(token.label)),
                    url = token.url,
                    title = null,
                )

            is BibliographyToken.Italic -> Emphasis(text = listOf(convert(token.token)))
            is BibliographyToken.Oblique -> Emphasis(text = listOf(convert(token.token)))
            is BibliographyToken.Bold -> Strong(text = listOf(convert(token.token)))
            is BibliographyToken.SmallCaps -> transform(token, TextTransformData(variant = TextTransformData.Variant.SMALL_CAPS))
            is BibliographyToken.Underline -> transform(token, TextTransformData(decoration = TextTransformData.Decoration.UNDERLINE))
            is BibliographyToken.Superscript -> transform(token, TextTransformData(script = TextTransformData.Script.SUP))
            is BibliographyToken.Subscript -> transform(token, TextTransformData(script = TextTransformData.Script.SUB))

            // Light font weight has no Quarkdown equivalent: the decorator is dropped.
            is BibliographyToken.Light -> convert(token.token)
        }

    /**
     * Wraps a decorated token's content in a [TextTransform] node applying [data].
     */
    private fun transform(
        token: BibliographyToken.Formatted,
        data: TextTransformData,
    ): TextTransform =
        TextTransform(
            data = data,
            text = listOf(convert(token.token)),
        )
}
