package eu.iamgio.quarkdown.lexer.impl

import eu.iamgio.quarkdown.lexer.EmphasisToken
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.lexer.StrongToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.walker.WalkerLexer

/**
 *
 */
class EmphasisLexer(source: CharSequence) : WalkerLexer("\n$source") {
    private var lastMatchIndex = 0

    private fun createToken(
        startDelimeter: StringBuilder,
        endDelimeter: StringBuilder,
        content: List<Token> = emptyList(),
    ): Token {
        val data =
            TokenData(
                text = buffer.toString(),
                position = lastMatchIndex until reader.index,
                groups = emptySequence(),
                // TODO groups
            )

        // Trims start delimeter to balance.
//        if (startDelimeter.length > endDelimeter.length) {
//            // TODO push removed delimeters as text
//            startDelimeter.delete(0, startDelimeter.length - endDelimeter.length)
//        }

        return when {
            startDelimeter.isEmpty() -> PlainTextToken(data)
            startDelimeter.length % 2 == 0 -> StrongToken(data, content)
            else -> EmphasisToken(data, content)
        }
    }

    private fun MutableList<Token>.push(token: Token) {
        // if (token.data.text.isEmpty()) {
        //    return
        // }

        this += token

        lastMatchIndex = reader.index
        buffer.clear()
    }

    // private fun MutableList<Token>.pushText() = push(StringBuilder(), StringBuilder())

    private fun MutableList<Token>.nextDelimeteredSequence(): Token {
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
        val sequence = nextSequence()

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

        return createToken(startDelimeterStreakBuffer, endDelimeterStreakBuffer, sequence)
    }

    private fun nextSequence() =
        buildList {
            while (true) {
                val next = reader.peek() ?: break
                // buffer.append(next)
                // if (next.isWhitespace()) {
                // reader.read()
                if (reader.peek() == '*') {
                    push(createToken(StringBuilder(), StringBuilder()))
                    push(nextDelimeteredSequence())
                    continue
                } else {
                    reader.peek()?.let {
                        buffer.append(it)
                    }
                }
                // }
                reader.read()
            }
            // push()
        }

    override fun tokenize(): List<Token> =
        buildList {
            addAll(nextSequence())
            push(createToken(StringBuilder(), StringBuilder()))
        }.also { println(it.joinToString(separator = "\n")) }

    override fun createFillToken(position: IntRange): Token {
        return PlainTextToken(
            TokenData(
                text = source.substring(position),
                position,
            ),
        )
    }
}
