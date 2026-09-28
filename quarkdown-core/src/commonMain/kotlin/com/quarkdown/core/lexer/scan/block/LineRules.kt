package com.quarkdown.core.lexer.scan.block

import com.quarkdown.core.lexer.scan.Line
import com.quarkdown.core.lexer.scan.MAX_BLOCK_INDENT
import com.quarkdown.core.lexer.scan.contentScanner

private const val UNORDERED_BULLETS = "*+-"

private const val FIRST_ORDERED_BULLET = '1'

private const val ORDERED_BULLET_TERMINATORS = ".)"

/**
 * Which ordered bullets count as a list marker.
 */
enum class OrderedBulletPolicy(
    /**
     * Highest amount of digits the marker may hold.
     */
    val maxDigits: Int,
) {
    /**
     * `1.` and `1)` only. CommonMark lets an ordered list interrupt a paragraph only when it starts at one,
     * which is what the paragraph and table interruption rules want.
     */
    ONE_ONLY(1),

    /**
     * Up to nine digits followed by `.` or `)`, which opens, continues and interrupts a list.
     */
    UP_TO_NINE_DIGITS(9),
}

/**
 * The line rules no single block owns: bullets, which lists, list items and setext headings all read, and
 * blankness. Every other opening rule belongs to the recognizer of the block it opens.
 */
object LineRules {
    /**
     * A tab-only line interrupts just as a space-only one does.
     * @return whether the line holds only whitespace, holds at least one character, and is terminated
     */
    fun isBlankTerminatedLine(line: Line): Boolean = line.isTerminated && !line.isEmpty && line.isBlank

    /**
     * @param line line to inspect
     * @param policy which ordered bullets count
     * @return the length of the bullet starting the line's content, or `null` when there is none
     */
    fun bulletLengthOf(
        line: Line,
        policy: OrderedBulletPolicy,
    ): Int? {
        if (line.indent > MAX_BLOCK_INDENT) return null
        val first = line.charAt(line.contentStart) ?: return null
        if (first in UNORDERED_BULLETS) return 1
        if (!first.isDigit()) return null
        val scanner = line.contentScanner()
        if (policy == OrderedBulletPolicy.ONE_ONLY && scanner.peek() != FIRST_ORDERED_BULLET) return null
        return scanner.attempt {
            val digits = takeWhile { it.isDigit() }
            if (digits !in 1..policy.maxDigits) return@attempt null
            if (!takeIf { it in ORDERED_BULLET_TERMINATORS }) return@attempt null
            digits + 1
        }
    }

    /**
     * A bullet interrupts only when one literal space follows it.
     * @param line line to inspect
     * @param policy which ordered bullets count
     * @return whether the line interrupts as a list item
     */
    fun isBulletInterruption(
        line: Line,
        policy: OrderedBulletPolicy,
    ): Boolean {
        val bullet = bulletLengthOf(line, policy) ?: return false
        return line.charAt(line.contentStart + bullet) == ' '
    }
}
