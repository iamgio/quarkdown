package com.quarkdown.core.lexer.scan

private const val GROUP_BEGIN = '('

private const val GROUP_END = ')'

/**
 * Consumes a destination enclosed in angle brackets, where it may hold spaces. A backslash escapes the next
 * character, so an escaped `>` stays inside; an unescaped `<` or a line break ends the destination, and the
 * brackets must close on the same line.
 * @param allowEmpty whether `<>`, which holds no destination at all, counts
 * @return the raw destination including its angle brackets, or `null` when none is there
 */
internal fun Scanner.takeAngleDestination(allowEmpty: Boolean): String? =
    attempt {
        val mark = mark()
        if (!take(DESTINATION_BEGIN)) return@attempt null
        val characters =
            repeatWhile {
                takeEscapedDestinationChar() || takeIf { it != '\n' && it != DESTINATION_BEGIN && it != DESTINATION_END }
            }
        if (!take(DESTINATION_END)) return@attempt null
        if (characters == 0 && !allowEmpty) return@attempt null
        sliceFrom(mark)
    }

/**
 * Consumes a destination that is not enclosed in angle brackets: printable characters up to the next
 * whitespace, where a backslash escapes the next character and parentheses come in balanced pairs. The one
 * caller that accepts an empty destination is the inline link, since `[l]()` parses.
 *
 * Link definitions and inline links read the same grammar here, so the rule lives in one place.
 *
 * @return the raw destination, empty when none starts here
 */
internal fun Scanner.takePlainDestination(): String {
    val mark = mark()
    repeatWhile { takeEscapedDestinationChar() || takeBalancedParentheses() || takeIf(::isPlainDestinationChar) }
    return sliceFrom(mark)
}

/**
 * Consumes an escape whose escaped character may appear in a destination, so a backslash before a space ends
 * a bare destination rather than extending it over the space.
 * @return whether an escape was consumed
 */
private fun Scanner.takeEscapedDestinationChar(): Boolean =
    step { take(ESCAPE) && takeIf { !CharCategories.isAsciiWhitespace(it) && it.code > LOWEST_PRINTABLE } }

/**
 * Consumes a parenthesised group whose parentheses balance at any depth. The depth is a counter, so a deeply
 * nested destination costs no stack.
 * @return whether a balanced group was consumed
 */
private fun Scanner.takeBalancedParentheses(): Boolean =
    step {
        if (!take(GROUP_BEGIN)) return@step false
        var depth = 1
        while (depth > 0) {
            when {
                takeEscapedDestinationChar() -> Unit
                take(GROUP_BEGIN) -> depth++
                take(GROUP_END) -> depth--
                takeIf(::isPlainDestinationChar) -> Unit
                else -> return@step false
            }
        }
        true
    }

/**
 * @param char character to classify
 * @return whether the character may appear in a destination outside a parenthesized group
 */
private fun isPlainDestinationChar(char: Char): Boolean =
    !CharCategories.isAsciiWhitespace(char) && char.code > LOWEST_PRINTABLE && char != GROUP_BEGIN && char != GROUP_END && char != ESCAPE
