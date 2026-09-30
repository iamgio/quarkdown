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
import com.quarkdown.core.ast.base.inline.SoftBreak
import com.quarkdown.core.ast.base.inline.Strikethrough
import com.quarkdown.core.ast.base.inline.Strong
import com.quarkdown.core.ast.base.inline.StrongEmphasis
import com.quarkdown.core.ast.base.inline.SubdocumentLink
import com.quarkdown.core.ast.base.inline.Text
import com.quarkdown.core.ast.quarkdown.inline.MathSpan
import com.quarkdown.core.ast.quarkdown.inline.TextSymbol
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.options.isSubdocumentUrl
import com.quarkdown.core.document.size.Size
import com.quarkdown.core.flavor.InlineLexerVariant
import com.quarkdown.core.function.value.factory.IllegalRawValueException
import com.quarkdown.core.function.value.factory.ValueFactory
import com.quarkdown.core.lexer.Lexer
import com.quarkdown.core.lexer.Token
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
import com.quarkdown.core.util.Escape
import com.quarkdown.core.util.trimDelimiters
import com.quarkdown.core.visitor.token.InlineTokenVisitor
import com.quarkdown.core.visitor.token.TokenVisitor

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
     * Parser the resolved children of an emphasis token are mapped through. The scanner nests tokens, so a
     * child may be any inline token, which this visitor covers and [InlineTokenVisitor] does not.
     */
    private val tokenParser: TokenVisitor<Node> by lazy { context.flavor.parserFactory.newParser(context) }

    /**
     * @return the parsed content of the tokenization from [this] lexer
     */
    private fun Lexer.tokenizeAndParse(): List<Node> =
        this
            .tokenize()
            .acceptAll(context.flavor.parserFactory.newParser(context))
            .toList()

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
            .newInlineLexer(source, variant = InlineLexerVariant.LINK_LABEL)
            .tokenizeAndParse()

    override fun visit(token: EscapeToken): Node = Text(text = token.character)

    override fun visit(token: EntityToken): Node {
        val entity = token.body.trim().lowercase()

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
                entity.startsWith("#x") -> token.numeric.orEmpty().decodeToContent(radix = 16)

                // Decimal (e.g. &#35)
                entity.startsWith("#") -> token.numeric.orEmpty().decodeToContent(radix = 10)

                // HTML entity (e.g. &nbsp;)
                else -> Escape.Html.unescape(token.data.text)
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

    override fun visit(token: LineBreakToken): Node = if (token.isHard) LineBreak else SoftBreak

    /**
     * Builds a link node, resolving it to a subdocument link when its URL points at a subdocument.
     * @param label raw label, parsed as link-label inline content
     * @param url raw destination
     * @param title raw title including its delimiters, if any
     * @return the link node
     */
    private fun linkNode(
        label: String,
        url: String,
        title: String?,
    ): LinkNode {
        val link =
            Link(
                label = parseLinkLabelSubContent(label),
                url = url.trim(),
                // Removes leading and trailing delimiters.
                title = title?.trimDelimiters()?.trim()?.let(::parseSubContent),
                fileSystem = context.fileSystem,
            )

        // The anchor is stripped from the URL, if present, to allow proper subdocument detection.
        // If the stripped URL points to a subdocument, it is a subdocument link.
        val result = link.stripAnchor()
        val strippedLink = result?.first ?: link
        val anchor = result?.second

        return when {
            context.isSubdocumentUrl(strippedLink.url) -> SubdocumentLink(strippedLink, anchor)
            else -> link
        }
    }

    /**
     * @param url the autolinked URL
     * @return a link whose label is its own URL
     */
    private fun autolinkNode(url: String): Node =
        Link(
            label = listOf(Text(url)),
            url = url,
            title = null,
        )

    override fun visit(token: LinkToken): LinkNode = linkNode(token.label, token.url, token.title)

    /**
     * Builds a reference link node, whose reference falls back to its own label when collapsed.
     * @param token token the node falls back to the text of
     * @param label raw label
     * @param reference raw reference, if the second bracket pair declared one
     * @return the reference link node
     */
    private fun referenceLinkNode(
        token: Token,
        label: String,
        reference: String?,
    ): ReferenceLink {
        val labelContent = parseLinkLabelSubContent(label)
        // When the reference is collapsed, the label is the same as the reference label.
        return ReferenceLink(
            label = labelContent,
            referenceLabel = reference?.let { parseLinkLabelSubContent(it) } ?: labelContent,
            fallback = { Text(token.data.text) },
        )
    }

    override fun visit(token: ReferenceLinkToken): ReferenceLink = referenceLinkNode(token, token.label, token.reference)

    override fun visit(token: ReferenceFootnoteToken): Node {
        val label = token.label
        val definition = token.definition

        return when {
            // All-in-one case:
            // Named: [^label: definition]
            // Anonymous: [^: definition]
            definition != null -> {
                ReferenceDefinitionFootnote(
                    label.takeUnless { it.isBlank() } ?: context.newUuid(),
                    definition = parseSubContent(definition),
                )
            }

            // Reference only case.
            else -> {
                ReferenceFootnote(
                    label,
                    fallback = { Text(token.data.text) },
                )
            }
        }
    }

    override fun visit(token: DiamondAutolinkToken): Node = autolinkNode(token.url.trim())

    override fun visit(token: UrlAutolinkToken): Node = autolinkNode(token.data.text.trim())

    /**
     * Parses an image's raw size into sizes.
     * @param width raw width, if any
     * @param height raw height, if any
     * @return pair of width and height, each `null` when unset or invalid
     */
    private fun imageSize(
        width: String?,
        height: String?,
    ): Pair<Size?, Size?> {
        fun toSize(raw: String?): Size? =
            try {
                raw?.let(ValueFactory::size)?.unwrappedValue // Parses the value.
            } catch (_: IllegalRawValueException) {
                null
            }

        return toSize(width) to toSize(height)
    }

    override fun visit(token: ImageToken): Node {
        val (width, height) = imageSize(token.width, token.height)
        return Image(linkNode(token.label, token.url, token.title), width, height, token.customId?.trim())
    }

    override fun visit(token: ReferenceImageToken): Node {
        val (width, height) = imageSize(token.width, token.height)
        val link = referenceLinkNode(token, token.label, token.reference)
        return ReferenceImage(link, width, height, token.customId?.trim())
    }

    override fun visit(token: CodeSpanToken): Node {
        val rawText = token.content.replace("\n", " ")

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
     * Emphasis is the one kind whose inner content is *not* re-lexed: the inline scanner resolves its nesting
     * with a delimiter stack and hands the resolved tokens over, which is what makes the nesting correct.
     * @param children the emphasis token's resolved children
     * @return the emphasis' inner content
     */
    private fun emphasisContent(children: List<Token>): InlineContent = children.map { it.accept(tokenParser) }

    override fun visit(token: EmphasisToken): Node = Emphasis(emphasisContent(token.children))

    override fun visit(token: StrongToken): Node = Strong(emphasisContent(token.children))

    override fun visit(token: StrongEmphasisToken): Node = StrongEmphasis(emphasisContent(token.children))

    override fun visit(token: StrikethroughToken): Node = Strikethrough(emphasisContent(token.children))

    override fun visit(token: InlineMathToken): Node = MathSpan(expression = token.expression.trim())
}
