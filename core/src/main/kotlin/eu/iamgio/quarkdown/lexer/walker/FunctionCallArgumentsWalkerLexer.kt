package eu.iamgio.quarkdown.lexer.walker

import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.regex.NamedToken
import eu.iamgio.quarkdown.lexer.tokens.PlainTextToken

/**
 * Beginning of a function argument.
 */
private const val ARG_DELIMITER_OPEN = '{'

/**
 * End of a function argument.
 */
private const val ARG_DELIMITER_CLOSE = '}'

/**
 * Minimum amount of spaces required by the body argument.
 */
private const val MIN_BODY_INDENTATION = 2

/**
 * A character-by-character lexer that tokenizes arguments of a function call,
 * in a way that preserves a balanced amount of brackets on both sides.
 * @param source source code to be tokenized, sliced right after the function name
 * @param allowsBody whether the function call allows a body argument
 */
class FunctionCallArgumentsWalkerLexer(
    source: CharSequence,
    private val allowsBody: Boolean,
) : WalkerLexer(source) {
    override fun createFillToken(position: IntRange): Token? = null

    /**
     * @return whether the current line begins satisfies one of the following:
     * - Begins with two or more spaces.
     * - Begins with a tab character.
     * - Is a blank line.
     */
    private fun hasEnoughIndentation(): Boolean {
        var whitespaces = 0
        while (true) {
            val char = reader.peek() ?: break

            // Empty lines or one tab indentation are always accepted.
            if (char == '\n' || char == '\t') {
                return true
            }
            // Space indentation is accepted if it is at least two spaces.
            if (char == ' ') {
                whitespaces++
                if (whitespaces >= MIN_BODY_INDENTATION) {
                    return true
                }
                reader.read()
                continue
            }
            // Any other character is not part of the indentation.
            break
        }

        return false
    }

    /**
     * @return a token containing the body argument of the call.
     */
    private fun readBody(): Token {
        while (true) {
            val char = reader.peek() ?: break

            reader.read()

            if (char == '\n') {
                println("newline")
                if (!hasEnoughIndentation()) {
                    // End of the body argument.
                    break
                }
            }

            buffer.append(char)
        }

        return NamedToken("bodyarg", super.createTokenDataFromBuffer())
    }

    override fun tokenize(): List<Token> =
        buildList {
            // The current depth inside nested arguments.
            // depth=0 {depth=1 {depth=2} depth=1} depth=0
            var depth = 0

            while (true) {
                // Current character being read.
                val char = reader.peek() ?: break

                // Spaces between arguments are ignored.
                if (char.isWhitespace() && depth == 0) {
                    reader.read()
                    if (char == '\n' && allowsBody) {
                        // Content indented by two spaces is treated as a body argument.
                        if (hasEnoughIndentation()) {
                            this += readBody()
                            break
                        }
                    }
                    continue
                }
                // If an argument delimiter is escaped, it is trated as a regular character.
                // If the escaped character is not an argument delimiter, its behavior is delegated to the parser.
                if (char == '\\') {
                    reader.read()
                    val next = reader.peek()
                    if (next == ARG_DELIMITER_OPEN || next == ARG_DELIMITER_CLOSE) {
                        buffer.append(next)
                        reader.read()
                    }
                    continue
                }

                // The content is buffered until the end of the argument.
                buffer.append(char)

                when {
                    // Beginning of an argument or nested argument.
                    char == ARG_DELIMITER_OPEN -> {
                        depth++
                    }

                    char == ARG_DELIMITER_CLOSE -> {
                        if (depth == 0) {
                            // {Argument}} <-- the last '}' is not part of the argument.
                            break
                        }
                        depth--
                        if (depth == 0) {
                            // End of the outer argument.
                            // The buffer is cleared and the argument is pushed.
                            this += PlainTextToken(super.createTokenDataFromBuffer())
                        }
                    }

                    // Any other character outside of delimiters:
                    // end of the function call.
                    depth == 0 -> {
                        break
                    }
                }

                // Read the next character.
                reader.read()
            }
        }
}
