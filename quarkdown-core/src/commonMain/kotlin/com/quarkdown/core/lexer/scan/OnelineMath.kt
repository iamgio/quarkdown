package com.quarkdown.core.lexer.scan

/**
 * Delimits a math expression, alone or in a fence run.
 */
internal const val MATH_DELIMITER = '$'

/**
 * Consumes a one-line math expression. It opens with a dollar sign and one space or tab and closes with one
 * space or tab and a dollar sign; an inner dollar sign belongs to the expression as long as it is not
 * adjacent to whitespace.
 *
 * The closing delimiter is the first candidate [accepts] agrees with. In `$ Math $expression $` the inner
 * dollar sign looks like a closing delimiter, and only the caller's trailing rule can reject it.
 *
 * @param accepts run just past a candidate closing delimiter, returning whether what follows is acceptable
 * @return the expression between the delimiters, its spacing excluded, or `null` when none starts here
 */
fun Scanner.takeOnelineMath(accepts: Scanner.() -> Boolean = { true }): String? =
    attempt {
        if (!take(MATH_DELIMITER) || !takeIf { it == ' ' || it == '\t' }) return@attempt null
        val mark = mark()
        while (!isAtEnd) {
            when {
                peek() == '\n' -> return@attempt null
                peek() != MATH_DELIMITER -> advance()
                else -> {
                    if (closesOnelineMath(mark)) {
                        val expression = sliceFrom(mark).dropLast(1)
                        val closed = attempt { if (take(MATH_DELIMITER) && accepts()) expression else null }
                        if (closed != null) return@attempt closed
                    }
                    if (!isCrossableDelimiter()) return@attempt null
                    advance()
                }
            }
        }
        null
    }

/**
 * A dollar sign that closes nothing belongs to the expression only when it has a non-whitespace neighbour.
 * One wrapped in whitespace ends the scan, and the line is not math at all.
 * @return whether the dollar sign the scanner sits on may be part of the expression
 */
private fun Scanner.isCrossableDelimiter(): Boolean =
    previous()?.let(CharCategories::isAsciiWhitespace) == false || peek(1)?.let(CharCategories::isAsciiWhitespace) == false

/**
 * @param contentStart index the expression's content starts at
 * @return whether the scanner sits on a closing delimiter: a dollar sign preceded by exactly one space or tab
 */
private fun Scanner.closesOnelineMath(contentStart: Int): Boolean =
    peek() == MATH_DELIMITER &&
        index - 2 >= contentStart &&
        (peek(-1) == ' ' || peek(-1) == '\t') &&
        peek(-2) != ' ' &&
        peek(-2) != '\t'
