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
 * End of named argument's name.
 */
private const val NAMED_ARG_DELIMITER_CLOSE = ':'

/**
 * Minimum amount of spaces required by the body argument.
 */
private const val MIN_BODY_INDENTATION = 2

/**
 * A character-by-character lexer that tokenizes arguments of a function call,
 * in a way that preserves a balanced amount of brackets on both sides.
 * Arguments are wrapped in brackets: `{arg}`, can be nested in balanced brackets: `{arg {x} arg}`,
 * they are separated by zero or more whitespaces, and can also be named: `name:{arg}` (with no whitespaces in-between).
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

            // Any other character is not part of the indentation.
            if (char != ' ') break

            // Space indentation is accepted if it is at least two spaces.
            whitespaces++
            if (whitespaces >= MIN_BODY_INDENTATION) {
                return true
            }

            reader.read()
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
                if (!hasEnoughIndentation()) {
                    // End of the body argument.
                    break
                }
            }

            buffer.append(char)
        }

        return NamedToken("bodyarg", super.createTokenDataFromBuffer())
    }

    /**
     * @return a token containing the name of the argument which is about to be read, or `null` if there is none.
     */
    private fun readArgName(): Token? {
        buffer.clear()

        while (true) {
            val char = reader.peek() ?: break

            // A name cannot contain spaces.
            if (char.isWhitespace()) break

            reader.read()

            // End of the name.
            if (char == NAMED_ARG_DELIMITER_CLOSE && reader.peek() == ARG_DELIMITER_OPEN) {
                return PlainTextToken(super.createTokenDataFromBuffer())
            }

            buffer.append(char)
        }

        buffer.clear()
        return null
    }

    override fun tokenize(): List<Token> =
        buildList {
            // The current depth inside nested arguments.
            // depth=0 {depth=1 {depth=2} depth=1} depth=0
            var depth = 0

            // Index to roll back to when the scanned content is not part of the function call anymore.
            // After this sub-lexer is finished, the main lexing resumes from this index.
            var rollbackIndex = 0

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
                        // Content after the argument keeps being scanned.
                        // In case there is no next argument, the lexer stops
                        // and the lexing resumes from this index, which is the end of the function call.
                        rollbackIndex = reader.index + 1

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

                    // Any other character outside of delimiters.
                    // It could be a named argument (e.g. name:{arg}),
                    // but if it's not, the lexer stops.
                    depth == 0 -> {
                        // val startIndex = reader.index
                        val argName = readArgName()
                        if (argName != null) {
                            this += argName
                            continue
                        } else {
                            // The text outside the delimiters does not represent a named argument:
                            // end of the function call.
                            reader.index = rollbackIndex // Rollback: the main lexing resumes from this index.
                            break
                        }
                    }
                }

                // Read the next character.
                reader.read()
            }
        }.also {
            // If the lexer has not produced any tokens, the result is discarded
            // and the lexing operation is not affected by this walking.
            if (it.isEmpty()) {
                reader.index = 0
            }
        }
}
