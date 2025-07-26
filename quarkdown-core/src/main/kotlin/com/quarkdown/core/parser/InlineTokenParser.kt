package com.quarkdown.core.parser

import com.quarkdown.core.ast.InlineContent
import com.quarkdown.core.ast.Node
import com.quarkdown.core.ast.base.LinkNode
import com.quarkdown.core.ast.base.inline.CodeSpan
import com.quarkdown.core.ast.base.inline.Comment
import com.quarkdown.core.ast.base.inline.CriticalContent
import com.quarkdown.core.ast.base.inline.Emphasis
import com.quarkdown.core.ast.base.inline.Image
import com.quarkdown.core.ast.base.inline.LineBreak
import com.quarkdown.core.ast.base.inline.Link
import com.quarkdown.core.ast.base.inline.ReferenceDefinitionFootnote
import com.quarkdown.core.ast.base.inline.ReferenceFootnote
import com.quarkdown.core.ast.base.inline.ReferenceImage
import com.quarkdown.core.ast.base.inline.ReferenceLink
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.ast.quarkdown.inline.TextSymbol
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.function.value.factory.IllegalRawValueException
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.TokenData
import com.quarkdown.core.lexer.acceptAll
import com.quarkdown.core.lexer.tokens.CodeSpanToken
import com.quarkdown.core.lexer.tokens.CommentToken
import com.quarkdown.core.lexer.tokens.CriticalContentToken
import com.quarkdown.core.lexer.tokens.DiamondAutolinkToken
import com.quarkdown.core.lexer.tokens.EmphasisToken
import com.quarkdown.core.lexer.tokens.EntityToken
import com.quarkdown.core.lexer.tokens.EscapeToken
import com.quarkdown.core.lexer.tokens.ImageToken
import com.quarkdown.core.lexer.tokens.InlineMathToken
import com.quarkdown.core.lexer.tokens.LineBreakToken
import com.quarkdown.core.lexer.tokens.LinkToken
import com.quarkdown.core.lexer.tokens.PlainTextToken
import com.quarkdown.core.lexer.tokens.ReferenceFootnoteToken
import com.quarkdown.core.lexer.tokens.ReferenceImageToken
import com.quarkdown.core.lexer.tokens.ReferenceLinkToken
import com.quarkdown.core.lexer.tokens.StrikethroughToken
import com.quarkdown.core.lexer.tokens.StrongEmphasisToken
import com.quarkdown.core.lexer.tokens.StrongToken
import com.quarkdown.core.lexer.tokens.TextSymbolToken
import com.quarkdown.core.lexer.tokens.UrlAutolinkToken
import com.quarkdown.core.misc.color.Color
import com.quarkdown.core.misc.color.decoder.HexColorDecoder
import com.quarkdown.core.misc.color.decoder.HsvHslColorDecoder
import com.quarkdown.core.misc.color.decoder.RgbColorDecoder
import com.quarkdown.core.misc.color.decoder.RgbaColorDecoder
import com.quarkdown.core.misc.color.decoder.decode
import com.quarkdown.core.util.iterator
import com.quarkdown.core.util.nextOrNull
import com.quarkdown.core.util.trimDelimiters
import com.quarkdown.core.visitor.token.InlineTokenVisitor
import org.apache.commons.text.StringEscapeUtils

/**
 * ASCII of the character that replaces null characters,
 * following CommonMark's security guideline _(2.3 Insecure characters)_.
 */
private const val NULL_CHAR_REPLACEMENT_ASCII = 65533

/**
 * A parser for inline tokens.
 * @param context additional data to fill during the parsing process
 */
class InlineTokenParser(
    private val context: MutableContext,
) : InlineTokenVisitor<Node> {
    /**
     * @return the parsed content of the tokenization from [this] lexer
     */
    private fun Lexer.tokenizeAndParse(): List<Node> =
        this
            .tokenize()
            .acceptAll(context.flavor.parserFactory.newParser(context))

    /**
     * Tokenizes and parses sub-nodes.
     * @param source source to tokenize using the default inline lexer from this flavor
     * @return parsed nodes
     */
    private fun parseSubContent(source: CharSequence) =
        context.flavor.lexerFactory
            .newInlineLexer(source)
            .tokenizeAndParse()

    /**
     * Tokenizes and parses sub-nodes within a link label.
     * @param source source to tokenize using the link label inline lexer from this flavor
     * @return parsed nodes
     */
    private fun parseLinkLabelSubContent(source: CharSequence) =
        context.flavor.lexerFactory
            .newLinkLabelInlineLexer(source)
            .tokenizeAndParse()

    override fun visit(token: EscapeToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return Text(text = groups.next())
    }

    override fun visit(token: EntityToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val entity = groups.next().trim().lowercase()

        /**
         * @param radix radix to decode the numeric value for (`radix = 10` for decimal, `radix = 16` for hexadecimal)
         * @return [this] string to its corresponding character in [radix] representation.
         */
        fun String.decodeToContent(radix: Int): String {
            val ascii = toIntOrNull(radix) ?: return ""
            // CommonMark's security guideline (2.3 Insecure characters)
            return if (ascii != 0) {
                ascii.toChar()
            } else {
                NULL_CHAR_REPLACEMENT_ASCII.toChar()
            }.toString()
        }

        // Critical because further checks and mappings may be required during the rendering stage.
        return CriticalContent(
            when {
                entity == "colon" -> ":"
                // Hexadecimal (e.g. &#xD06)
                entity.startsWith("#x") -> groups.next().decodeToContent(radix = 16)
                // Decimal (e.g. &#35)
                entity.startsWith("#") -> groups.next().decodeToContent(radix = 10)
                // HTML entity (e.g. &nbsp;)
                else -> StringEscapeUtils.unescapeHtml4(token.data.text)
            },
        )
    }

    override fun visit(token: CriticalContentToken): Node = CriticalContent(token.data.text)

    override fun visit(token: TextSymbolToken): Node {
        // The symbol is then treated separately from text in the renderer.
        // e.g. the HTML renderer converts the symbol to its corresponding HTML entity (© -> &copy;).
        return TextSymbol(token.symbol.result)
    }

    override fun visit(token: CommentToken): Node {
        // Content is ignored.
        return Comment
    }

    override fun visit(token: LineBreakToken): Node = LineBreak

    override fun visit(token: LinkToken): LinkNode {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val link =
            Link(
                label = parseLinkLabelSubContent(groups.next()),
                url = groups.next().trim(),
                // Removes leading and trailing delimiters.
                title = groups.nextOrNull()?.trimDelimiters()?.trim(),
            )
        return when {
            // If the URL points to a subdocument, it is a subdocument link.
            link.url.endsWith(context.options.subdocumentUrlSuffix) -> SubdocumentLink(link)
            else -> link
        }
    }

    override fun visit(token: ReferenceLinkToken): ReferenceLink {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val label = parseLinkLabelSubContent(groups.next())
        // When the reference is collapsed, the label is the same as the reference label.
        return ReferenceLink(
            label = label,
            reference = groups.nextOrNull()?.let { parseLinkLabelSubContent(it) } ?: label,
            fallback = { Text(token.data.text) },
        )
    }

    override fun visit(token: ReferenceFootnoteToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val label = groups.next()
        val definition = groups.nextOrNull()

        return when {
            // All-in-one case:
            // Named: [^label: definition]
            // Anonymous: [^: definition]
            definition != null ->
                ReferenceDefinitionFootnote(
                    label.takeUnless { it.isBlank() } ?: context.newUuid(),
                    definition = parseSubContent(definition),
                )

            // Reference only case.
            else ->
                ReferenceFootnote(
                    label,
                    fallback = { Text(token.data.text) },
                )
        }
    }

    override fun visit(token: DiamondAutolinkToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        val url = groups.next().trim()
        return visit(UrlAutolinkToken(token.data.copy(text = url)))
    }

    override fun visit(token: UrlAutolinkToken): Node {
        val url = token.data.text.trim()
        return Link(
            label = listOf(Text(url)),
            url = url,
            title = null,
        )
    }

    /**
     * Given an image token, extracts its width and height, if they are set.
     * They are stored in the named groups `width` and `height`, both prefixed by [namedGroupPrefix].
     * @param namedGroupPrefix prefix of the named groups
     * @param data token data to extract the size from
     * @return pair of width and height, or `null` if they are either unset or invalid
     */
    private fun extractImageSize(
        namedGroupPrefix: String,
        data: TokenData,
    ): Pair<Size?, Size?> {
        val width = data.namedGroups["${namedGroupPrefix}width"]
        val height = data.namedGroups["${namedGroupPrefix}height"]

        fun toSize(raw: String?): Size? =
            try {
                raw?.let(ValueFactory::size)?.unwrappedValue // Parses the value.
            } catch (_: IllegalRawValueException) {
                null
            }

        return toSize(width) to toSize(height)
    }

    override fun visit(token: ImageToken): Node {
        val link = visit(LinkToken(token.data))
        val (width, height) = extractImageSize("img", token.data)

        return Image(link, width, height)
    }

    override fun visit(token: ReferenceImageToken): Node {
        val link = visit(ReferenceLinkToken(token.data))
        val (width, height) = extractImageSize("refimg", token.data)

        return ReferenceImage(link, width, height)
    }

    override fun visit(token: CodeSpanToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 3)
        val rawText = groups.next().replace("\n", " ")

        // If the text start and ends by a space, and does contain non-space characters,
        // the leading and trailing spaces are trimmed (according to CommonMark).
        val hasNonSpaceChars = rawText.any { it != ' ' }
        val hasSpaceCharsOnBothEnds = rawText.firstOrNull() == ' ' && rawText.lastOrNull() == ' '

        // Trimmed final text.
        val text =
            if (hasNonSpaceChars && hasSpaceCharsOnBothEnds) {
                rawText.trimDelimiters()
            } else {
                rawText
            }

        // Additional content brought by the code span.
        // If null, no additional content is present.
        val content: CodeSpan.ContentInfo? =
            // Color decoding. Named colors are disabled due to performance reasons.
            Color
                .decode(text, HexColorDecoder, RgbColorDecoder, RgbaColorDecoder, HsvHslColorDecoder)
                ?.let(CodeSpan::ColorContent)

        return CodeSpan(text, content)
    }

    override fun visit(token: PlainTextToken): Node = Text(token.data.text)

    /**
     * @param token emphasis token to parse the content for
     * @return parsed content of an emphasis token
     */
    private fun emphasisContent(token: Token): InlineContent {
        // The raw string content, without the delimiters.
        val text =
            token.data.groups
                .iterator(consumeAmount = 3)
                .next()
        return parseSubContent(text)
    }

    override fun visit(token: EmphasisToken): Node = Emphasis(emphasisContent(token))

    override fun visit(token: StrongToken): Node = Strong(emphasisContent(token))

    override fun visit(token: StrongEmphasisToken): Node = StrongEmphasis(emphasisContent(token))

    override fun visit(token: StrikethroughToken): Node = Strikethrough(emphasisContent(token))

    override fun visit(token: InlineMathToken): Node {
        val groups = token.data.groups.iterator(consumeAmount = 2)
        return MathSpan(expression = groups.next().trim())
    }
}
