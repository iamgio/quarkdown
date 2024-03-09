package eu.iamgio.quarkdown.lexer.impl

import eu.iamgio.quarkdown.lexer.AbstractLexer
import eu.iamgio.quarkdown.lexer.EmphasisToken
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.lexer.StrongToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.regex.pattern.TokenRegexPattern
import kotlin.math.min

/**
 *
 */
class EmphasisLexer(
    source: CharSequence,
    private val leftDelimeterPattern: TokenRegexPattern,
    private val rightDelimeterPattern: TokenRegexPattern,
    // TODO multiple right delimeters
) : AbstractLexer(source) {
    private fun MutableList<Token>.nextSequence(): Token? {
        // The left delimeter (e.g. **).
        val startMatch = leftDelimeterPattern.regex.find(source, startIndex = super.currentIndex) ?: return null

        // The right delimeter + the content (e.g. some text**).
        val endMatch =
            rightDelimeterPattern.regex.find(
                source.removeRange(startMatch.range.first, startMatch.range.last + 1),
                startIndex = super.currentIndex,
            ) ?: return null

        // The left delimeter as a string.
        val leftDelimeter = startMatch.value
        // The right delimeter as a string.
        val rightDelimeter = endMatch.groups[2]?.value ?: return null
        // The amount of delimeter characters on either side.
        val delimeterLength = min(leftDelimeter.length, rightDelimeter.length)

        // The complete range of the token.
        // The source string substringed to this range goes from the left delimeter to the right delimeter (inclusive).
        val range = (startMatch.range.first)..(endMatch.range.last + leftDelimeter.length)

        if (range.last > source.length || range.first > source.length) {
            return null
        }

        pushFillToken(untilIndex = range.first)

        // The token data.
        val data =
            TokenData(
                text = source.substring(range),
                position = range,
                groups =
                    sequence {
                        yieldAll(startMatch.groupValues)
                        yieldAll(endMatch.groupValues)
                    },
            )

        currentIndex = endMatch.range.last + leftDelimeter.length + 1

        val wrap =
            when {
                delimeterLength % 2 != 0 -> ::EmphasisToken
                else -> ::StrongToken
            }

        return wrap(data)
    }

    override fun tokenize(): List<Token> =
        buildList {
            var next: Token? = null
            while (nextSequence().also { next = it } != null) {
                add(next!!)
            }
            // add(nextSequence()!!) // TODO debug
            pushFillToken(untilIndex = source.length)
        }

    override fun createFillToken(position: IntRange): Token {
        return PlainTextToken(
            TokenData(
                text = source.substring(position),
                position,
            ),
        )
    }
}
