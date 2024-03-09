package eu.iamgio.quarkdown.lexer.impl

import eu.iamgio.quarkdown.lexer.EmphasisToken
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.walker.WalkerLexer

/**
 *
 */
class EmphasisLexer(source: CharSequence) : WalkerLexer("\n$source") {
    private var lastMatchIndex = 0

    private var startDelimeterStreakBuffer = StringBuilder()
    private var endDelimeterStreakBuffer = StringBuilder()

    private fun MutableList<Token>.push() {
        if (buffer.isEmpty()) {
            return
        }

        // The token data.
        val data =
            TokenData(
                text = buffer.toString(),
                position = lastMatchIndex until reader.index,
                groups = emptySequence(),
                // TODO groups
            )

        // Trims start delimeter to balance.
        if (startDelimeterStreakBuffer.length > endDelimeterStreakBuffer.length) {
            // TODO push removed delimeters as text
            startDelimeterStreakBuffer.delete(0, startDelimeterStreakBuffer.length - endDelimeterStreakBuffer.length)
        }

        val wrap: (TokenData) -> Token =
            when {
                startDelimeterStreakBuffer.isEmpty() -> ::PlainTextToken
                startDelimeterStreakBuffer.length % 2 == 0 -> ::StrongEmphasisToken
                else -> ::EmphasisToken
            }

        lastMatchIndex = reader.index
        buffer.clear()
        startDelimeterStreakBuffer.clear()
        endDelimeterStreakBuffer.clear()

        this += wrap(data)
    }

    private fun MutableList<Token>.nextDelimeteredSequence() {
        // Left delimeter
        while (true) {
            val next = reader.peek()?.takeIf { it == '*' }
            if (next == null) {
                break
            }
            startDelimeterStreakBuffer.append(next)
            reader.read()
        }

        // Content
        // TODO should be nextSequence() for proper recursion
        while (true) {
            val next = reader.peek()?.takeIf { it != '*' }
            /*if (next == '\\' && reader.peek() == '*') {
                reader.read() // Escape
            }*/
            if (next == null) {
                break
            }
            buffer.append(next)
            reader.read()
        }

        // Right delimeter
        while (true) {
            val next = reader.peek()?.takeIf { it == '*' }
            // Trims end delimeter to balance.
            if (next == null || endDelimeterStreakBuffer.length >= startDelimeterStreakBuffer.length) {
                break
            }
            endDelimeterStreakBuffer.append(next)
            reader.read()
        }

        push()
    }

    private fun MutableList<Token>.nextSequence() {
        while (true) {
            val next = reader.peek() ?: break
            if (next == '*') {
                push()
                nextDelimeteredSequence()
                continue
            }
            buffer.append(next)
            reader.read()
        }
        push()
    }

    override fun tokenize(): List<Token> =
        buildList {
            nextSequence()
            push()
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
