package com.quarkdown.core.lexer.scan.block

import com.quarkdown.core.lexer.scan.LineCursor
import com.quarkdown.core.lexer.scan.block.base.BlockQuoteRecognizer
import com.quarkdown.core.lexer.scan.block.base.FencedCodeRecognizer
import com.quarkdown.core.lexer.scan.block.base.HeadingRecognizer
import com.quarkdown.core.lexer.scan.block.base.TableRecognizer
import com.quarkdown.core.lexer.scan.block.base.ThematicBreakRecognizer
import com.quarkdown.core.lexer.scan.block.quarkdown.MultilineMathRecognizer

/**
 * A single reason a line interrupts an open block.
 */
fun interface InterruptionRule {
    /**
     * @param cursor the line to test
     * @return whether the line interrupts
     */
    fun interrupts(cursor: LineCursor): Boolean
}

/**
 * The reasons a line interrupts an open block. Paragraphs, block quotes, footnote definitions, tables and
 * lists each take a variant of this set.
 * @param rules the reasons, any of which is enough
 */
class Interruption(
    private val rules: List<InterruptionRule>,
) {
    /**
     * @param cursor the line to test
     * @return whether any rule interrupts at the line
     */
    fun interrupts(cursor: LineCursor): Boolean {
        for (index in rules.indices) {
            if (rules[index].interrupts(cursor)) return true
        }
        return false
    }

    /**
     * @param rule additional reason a flavor introduces
     * @return a copy of this interruption with [rule] appended
     */
    operator fun plus(rule: InterruptionRule) = Interruption(rules + rule)
}

/**
 * The interruption variants of the base Markdown flavor.
 */
object BaseInterruptions {
    private val thematicBreak = InterruptionRule { ThematicBreakRecognizer.opensAt(it.current) }
    private val heading = InterruptionRule { HeadingRecognizer.opensAt(it.current) }
    private val blockQuote = InterruptionRule { BlockQuoteRecognizer.opensAt(it.current) }
    private val fence = InterruptionRule { FencedCodeRecognizer.opensAt(it.current) }
    private val table = InterruptionRule { TableRecognizer.opensAt(it) }
    private val blankLine = InterruptionRule { LineRules.isBlankTerminatedLine(it.current) }

    private fun bullet(policy: OrderedBulletPolicy) = InterruptionRule { LineRules.isBulletInterruption(it.current, policy) }

    private val shared = listOf(thematicBreak, heading, blockQuote, fence, blankLine)

    /**
     * What interrupts a paragraph, a block quote and a footnote definition. Only an ordered list starting at
     * one may interrupt a paragraph, per CommonMark.
     */
    val paragraph = Interruption(shared + bullet(OrderedBulletPolicy.ONE_ONLY) + table)

    /**
     * What interrupts a table's cell rows: the paragraph rules without the table rule.
     */
    val tableRows = Interruption(shared + bullet(OrderedBulletPolicy.ONE_ONLY))

    /**
     * What interrupts a list: the paragraph rules where any bullet counts, ordered or not.
     */
    val list = Interruption(shared + bullet(OrderedBulletPolicy.UP_TO_NINE_DIGITS) + table)
}

/**
 * The interruption variants of the Quarkdown flavor, which adds multiline math to the base ones.
 */
object QuarkdownInterruptions {
    private val multilineMath = InterruptionRule { MultilineMathRecognizer.opensAt(it.current) }

    /**
     * What interrupts a paragraph, a block quote and a footnote definition.
     */
    val paragraph = BaseInterruptions.paragraph + multilineMath

    /**
     * What interrupts a table's cell rows.
     */
    val tableRows = BaseInterruptions.tableRows + multilineMath

    /**
     * What interrupts a list.
     */
    val list = BaseInterruptions.list + multilineMath
}
