package eu.iamgio.quarkdown.lexer.walker

import eu.iamgio.quarkdown.lexer.Token
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
 * A character-by-character lexer that tokenizes arguments of a function call,
 * in a way that preserves a balanced amount of brackets on both sides.
 */
class FunctionCallArgumentsWalkerLexer(source: CharSequence) : WalkerLexer(source) {
    override fun createFillToken(position: IntRange): Token? = null

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

                    // TODO body argument

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
