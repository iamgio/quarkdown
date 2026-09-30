package com.quarkdown.core.lexer.scan.inline

import com.quarkdown.core.lexer.scan.CharCategories
import com.quarkdown.core.lexer.scan.DESTINATION_BEGIN
import com.quarkdown.core.lexer.scan.Scanner
import com.quarkdown.core.lexer.scan.takeAngleDestination
import com.quarkdown.core.lexer.scan.takeDelimitedTitle
import com.quarkdown.core.lexer.scan.takePlainDestination

private const val TARGET_BEGIN = '('

private const val TARGET_END = ')'

/**
 * A link destination with its optional title.
 * @param url the raw destination, angle brackets included when it had them
 * @param title the raw title including its delimiters, if any
 */
class TargetMatch(
    val url: String,
    val title: String?,
)

/**
 * Consumes a parenthesized destination with its optional title.
 * @return the target, or `null` when none starts here
 */
fun Scanner.takeLinkTarget(): TargetMatch? =
    attempt {
        if (!take(TARGET_BEGIN)) return@attempt null
        takeWhile(CharCategories::isAsciiWhitespace)
        // A destination that opens with an angle bracket has to close with one: `[l](<u)` is no link.
        val url =
            when (peek()) {
                DESTINATION_BEGIN -> takeAngleDestination(allowEmpty = false) ?: return@attempt null
                else -> takePlainDestination()
            }
        val title = takeTargetTitle()
        takeWhile(CharCategories::isAsciiWhitespace)
        if (take(TARGET_END)) TargetMatch(url, title) else null
    }

/**
 * A title needs at least one space before it.
 * @return the raw title including its delimiters, or `null` when there is none
 */
private fun Scanner.takeTargetTitle(): String? =
    attempt {
        if (takeWhile(CharCategories::isAsciiWhitespace) == 0) return@attempt null
        takeDelimitedTitle()
    }
