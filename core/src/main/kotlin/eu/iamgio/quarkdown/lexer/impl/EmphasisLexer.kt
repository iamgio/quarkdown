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

    private fun MutableList<Token>.push(
        startDelimeter: StringBuilder,
        endDelimeter: StringBuilder,
    ) {
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
        if (startDelimeter.length > endDelimeter.length) {
            // TODO push removed delimeters as text
            startDelimeter.delete(0, startDelimeter.length - endDelimeter.length)
        }

        val wrap: (TokenData) -> Token =
            when {
                startDelimeter.isEmpty() -> ::PlainTextToken
                startDelimeter.length % 2 == 0 -> ::StrongEmphasisToken
                else -> ::EmphasisToken
            }

        lastMatchIndex = reader.index
        buffer.clear()
        startDelimeter.clear()
        endDelimeter.clear()

        this += wrap(data)
    }

    private fun MutableList<Token>.pushText() = push(StringBuilder(), StringBuilder())

    private fun MutableList<Token>.nextDelimeteredSequence() {
        val startDelimeterStreakBuffer = StringBuilder()
        val endDelimeterStreakBuffer = StringBuilder()

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

        push(startDelimeterStreakBuffer, endDelimeterStreakBuffer)
    }

    private fun MutableList<Token>.nextSequence() {
        while (true) {
            val next = reader.peek() ?: break
            buffer.append(next)
            if (next.isWhitespace()) {
                reader.read()
                if (reader.peek() == '*') {
                    pushText()
                    nextDelimeteredSequence()
                    continue
                } else {
                    reader.peek()?.let {
                        buffer.append(it)
                    }
                }
            }
            reader.read()
        }
        pushText()
    }

    override fun tokenize(): List<Token> =
        buildList {
            nextSequence()
            pushText()
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
