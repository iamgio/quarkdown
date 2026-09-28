package com.quarkdown.core.lexer.scan.inline.base

import com.quarkdown.core.lexer.scan.CharCategories
import com.quarkdown.core.lexer.scan.DESTINATION_BEGIN
import com.quarkdown.core.lexer.scan.DESTINATION_END
import com.quarkdown.core.lexer.scan.LOWEST_PRINTABLE
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.inline.InlineMatch
import com.quarkdown.core.lexer.scan.inline.InlineRecognizer
import com.quarkdown.core.lexer.scan.inline.inlineMatch
import com.quarkdown.core.lexer.tokens.DiamondAutolinkToken
import com.quarkdown.core.lexer.tokens.UrlAutolinkToken

private const val MAX_SCHEME_TAIL = 31

private const val MAX_DOMAIN_LABEL = 63

private const val SCHEME_MARKER = ':'

private const val EMAIL_MARKER = '@'

private const val LABEL_SEPARATOR = '.'

private const val LABEL_HYPHEN = '-'

private const val HOST_PUNCTUATION = "-_"

private const val SCHEME_PUNCTUATION = "+.-"

private const val LOCAL_PART_PUNCTUATION = "._+-"

private const val DIAMOND_LOCAL_PUNCTUATION = ".!#$%&'*+/=?^_`{|}~-"

private val URL_PREFIXES = listOf("http://", "https://", "ftp://", "www.")

private val URL_PREFIX_FIRST_CHARS = URL_PREFIXES.mapTo(mutableSetOf()) { it[0] }

/**
 * Recognizes a URL or an email address wrapped in angle brackets.
 */
object DiamondAutolinkRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        if (source.getOrNull(index) != DESTINATION_BEGIN) return null
        val scanner = Scanner(source, index = index)
        scanner.take(DESTINATION_BEGIN)
        val innerMark = scanner.mark()
        if (!scanner.takeSchemeUrl() && !scanner.takeDiamondEmail()) return null
        val url = scanner.sliceFrom(innerMark)
        if (!scanner.take(DESTINATION_END)) return null
        return inlineMatch(source, index, scanner.index) { DiamondAutolinkToken(it, url = url) }
    }
}

/**
 * Recognizes a bare URL or email address. The scheme prefixes are matched case insensitively.
 */
object UrlAutolinkRecognizer : InlineRecognizer {
    override fun recognize(
        source: CharSequence,
        index: Int,
    ): InlineMatch? {
        val scanner = Scanner(source, index = index)
        if (!scanner.takePrefixedUrl() && !scanner.takePlainEmail()) return null
        return inlineMatch(source, index, scanner.index, ::UrlAutolinkToken)
    }
}

/**
 * Consumes a scheme, which is a letter followed by up to [MAX_SCHEME_TAIL] scheme characters and a colon,
 * then everything up to whitespace, a control character or an angle bracket.
 * @return whether a scheme URL was consumed
 */
private fun Scanner.takeSchemeUrl(): Boolean =
    step {
        if (!takeIf { it in 'a'..'z' || it in 'A'..'Z' }) return@step false
        val tail = takeWhile { it.isSchemeChar() }
        if (tail !in 1..MAX_SCHEME_TAIL || !take(SCHEME_MARKER)) return@step false
        takeWhile { it.isDiamondUrlChar() }
        true
    }

/**
 * Consumes an address enclosed in angle brackets: a local part, an `@`, and a domain.
 * @return whether an address was consumed
 */
private fun Scanner.takeDiamondEmail(): Boolean = step { takeWhile { it.isDiamondLocalChar() } > 0 && take(EMAIL_MARKER) && takeDomain() }

/**
 * Consumes a bracketed address's domain: two or more dot-separated labels, each starting and ending with a
 * letter or a digit, and none longer than [MAX_DOMAIN_LABEL].
 * @return whether a domain was consumed
 */
private fun Scanner.takeDomain(): Boolean =
    step {
        if (!takeDomainLabel()) return@step false
        val more = repeatWhile { take(LABEL_SEPARATOR) && takeDomainLabel() }
        more > 0 && !peek().isHostPunctuation()
    }

/**
 * @return whether one domain label was consumed
 */
private fun Scanner.takeDomainLabel(): Boolean =
    step {
        val mark = mark()
        if (!takeIf { CharCategories.isAsciiLetterOrDigit(it) }) return@step false
        takeWhile { CharCategories.isAsciiLetterOrDigit(it) || it == LABEL_HYPHEN }
        index - mark <= MAX_DOMAIN_LABEL && previous()?.let(CharCategories::isAsciiLetterOrDigit) == true
    }

/**
 * Consumes a bare URL: one of [URL_PREFIXES], then at least one label character, then everything up to
 * whitespace or a `<`.
 * @return whether a URL was consumed
 */
private fun Scanner.takePrefixedUrl(): Boolean =
    step {
        if (peek()?.lowercaseChar() !in URL_PREFIX_FIRST_CHARS) return@step false
        if (URL_PREFIXES.none { take(it, ignoreCase = true) }) return@step false
        if (takeWhile { CharCategories.isAsciiLetterOrDigit(it) || it == LABEL_HYPHEN } == 0) return@step false
        takeWhile { !CharCategories.isAsciiWhitespace(it) && it != DESTINATION_BEGIN }
        true
    }

/**
 * Consumes a bare address, whose grammar is looser than a bracketed one's: its host labels need only end with
 * a letter or a digit.
 * @return whether an address was consumed
 */
private fun Scanner.takePlainEmail(): Boolean =
    step {
        if (takeWhile { it.isPlainLocalChar() } == 0 || !take(EMAIL_MARKER)) return@step false
        if (takeWhile { it.isPlainHostChar() } == 0) return@step false
        val dots =
            repeatWhile {
                take(LABEL_SEPARATOR) &&
                    takeWhile { it.isPlainHostChar() } > 0 &&
                    previous()?.let(CharCategories::isAsciiLetterOrDigit) == true
            }
        dots > 0 && !peek().isHostPunctuation()
    }

/**
 * The autolink grammars are ASCII throughout: `http://x.com` spelled with non-ASCII letters is not a URL.
 * @return whether the character is an ASCII letter or digit
 */
private fun Char.isSchemeChar(): Boolean = CharCategories.isAsciiLetterOrDigit(this) || this in SCHEME_PUNCTUATION

private fun Char.isDiamondUrlChar(): Boolean =
    !CharCategories.isAsciiWhitespace(this) && code > LOWEST_PRINTABLE && this != DESTINATION_BEGIN && this != DESTINATION_END

private fun Char.isDiamondLocalChar(): Boolean = CharCategories.isAsciiLetterOrDigit(this) || this in DIAMOND_LOCAL_PUNCTUATION

private fun Char.isPlainLocalChar(): Boolean = CharCategories.isAsciiLetterOrDigit(this) || this in LOCAL_PART_PUNCTUATION

private fun Char.isPlainHostChar(): Boolean = CharCategories.isAsciiLetterOrDigit(this) || this in HOST_PUNCTUATION

/**
 * @return whether the character is a host character that may not end a domain, the edges of the source
 *         counting as no such character
 */
private fun Char?.isHostPunctuation(): Boolean = this != null && this in HOST_PUNCTUATION
