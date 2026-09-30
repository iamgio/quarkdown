package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.Token
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.scan.takeCustomId
import com.quarkdown.core.lexer.tokens.ImageToken
import com.quarkdown.core.lexer.tokens.LinkToken
import com.quarkdown.core.lexer.tokens.ReferenceImageToken
import com.quarkdown.core.lexer.tokens.ReferenceLinkToken

private const val IMAGE_MARKER = '!'

private const val SIZE_BEGIN = '('

private const val SIZE_END = ')'

private const val SIZE_DIVIDERS = "* \t"

private const val SIZE_DIVIDER_LETTER = 'x'

/**
 * A parsed image size prefix and where it ends.
 * @param width raw width, if the prefix was present
 * @param height raw height, if the prefix declared one
 * @param end index just past the prefix
 */
private class SizeMatch(
    val width: String?,
    val height: String?,
    val end: Int,
)

/**
 * An image's parts, whatever kind of link it delegates to.
 * @param delegate the link or reference link token the image wraps
 * @param width raw width, if the size prefix was present and a link followed it
 * @param height raw height, if that prefix declared one
 * @param customId raw cross-reference identifier, if any
 * @param end index just past the whole image
 */
private class ImageParts(
    val delegate: Token,
    val width: String?,
    val height: String?,
    val customId: String?,
    val end: Int,
)

/**
 * Recognizes an image: a `!`, an optional size in parentheses, a link, and an optional cross-reference
 * identifier.
 */
object ImageRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        val parts = imageParts(source, index, LinkRecognizer) ?: return null
        val link = parts.delegate as LinkToken
        return inlineMatch(source, index, parts.end) {
            ImageToken(
                it,
                label = link.label,
                url = link.url,
                title = link.title,
                width = parts.width,
                height = parts.height,
                customId = parts.customId,
            )
        }
    }
}

/**
 * Recognizes an image whose destination lives in a link definition.
 */
object ReferenceImageRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        val parts = imageParts(source, index, ReferenceLinkRecognizer) ?: return null
        val reference = parts.delegate as ReferenceLinkToken
        return inlineMatch(source, index, parts.end) {
            ReferenceImageToken(
                it,
                label = reference.label,
                reference = reference.reference,
                width = parts.width,
                height = parts.height,
                customId = parts.customId,
            )
        }
    }
}

/**
 * Parses a `!`, an optional size prefix, a delegated link and an optional cross-reference identifier.
 * The size prefix counts only when a link follows it; otherwise the `!` is followed by a bare link.
 * @param source text to inspect
 * @param index index the `!` must sit at
 * @param delegate recognizer of the link the image wraps
 * @return the parts, or `null` when no image starts at [index]
 */
private fun imageParts(
    source: CharSequence,
    index: Int,
    delegate: InlineRecognizer,
): ImageParts? {
    if (source.getOrNull(index) != IMAGE_MARKER) return null
    val afterMarker = index + 1
    val size = imageSizeAt(source, afterMarker)
    val sized = size?.let { delegate.recognize(source, it.end) }
    val match = sized ?: delegate.recognize(source, afterMarker) ?: return null
    val scanner = Scanner(source, index = match.end)
    // Spaces before the identifier belong to it.
    val customId =
        scanner.attempt {
            takeSpacesAndTabs()
            takeCustomId()
        }
    return ImageParts(
        delegate = match.token,
        width = size?.width?.takeIf { sized != null },
        height = size?.height?.takeIf { sized != null },
        customId = customId,
        end = scanner.index,
    )
}

/**
 * Consumes an optional size prefix: the first `)` on the line closes it and the first divider inside splits
 * width from height.
 * @param source text to inspect
 * @param index index just past the `!`
 * @return the size, or `null` when no size prefix starts there
 */
private fun imageSizeAt(
    source: CharSequence,
    index: Int,
): SizeMatch? {
    val lineEnd = source.indexOf('\n', index).let { if (it < 0) source.length else it }
    val scanner = Scanner(source, limit = lineEnd, index = index)
    if (!scanner.take(SIZE_BEGIN)) return null
    val widthMark = scanner.mark()
    // The width holds at least one character, so a divider may not open it:
    // `!(x)[i](u)` is an image whose width is `x`, not one with an empty width.
    if (scanner.peek() == SIZE_END || !scanner.advance()) return null
    scanner.repeatWhile { peek() != SIZE_END && !isSizeDividerAhead() && advance() }
    val width = scanner.sliceFrom(widthMark)
    val height =
        scanner.attempt {
            if (!isSizeDividerAhead() || !advance()) return@attempt null
            val mark = mark()
            if (takeWhile { it != SIZE_END } == 0) return@attempt null
            sliceFrom(mark)
        }
    if (!scanner.take(SIZE_END)) return null
    return SizeMatch(width, height, scanner.index)
}

/**
 * A width and a height are separated by an asterisk, a space, a tab, or an `x` in either case that does not
 * follow a letter. That last condition keeps `1cmx1cm` from splitting at the `x`.
 * @return whether the scanner sits on a character that separates a width from a height
 */
private fun Scanner.isSizeDividerAhead(): Boolean =
    when (val char = peek()) {
        null -> false
        in SIZE_DIVIDERS -> true
        SIZE_DIVIDER_LETTER, SIZE_DIVIDER_LETTER.uppercaseChar() -> previous()?.isLetter() != true
        else -> false
    }
