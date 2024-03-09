package eu.iamgio.quarkdown.lexer.impl

import eu.iamgio.quarkdown.lexer.EmphasisToken
import eu.iamgio.quarkdown.lexer.PlainTextToken
import eu.iamgio.quarkdown.lexer.StrongEmphasisToken
import eu.iamgio.quarkdown.lexer.StrongToken
import eu.iamgio.quarkdown.lexer.Token
import eu.iamgio.quarkdown.lexer.TokenData
import eu.iamgio.quarkdown.lexer.walker.WalkerLexer

/**
 *
 */
class EmphasisLexer(
    source: CharSequence,
) : WalkerLexer(source) {
    enum class TokenType(val wrap: (TokenData) -> Token) {
        PLAIN(::PlainTextToken),
        STRONG(::StrongToken),
        EMPHASIS(::EmphasisToken),
        STRONG_EMPHASIS(::StrongEmphasisToken),
    }

    // TODO support multiple right delimeter types

    // TODO maybe override RegexLexer's extractMatchingToken?
    // The emphasis lexer matches the left delimeter and THEN searches for its right delimeter
    // or maybe just tweak the delimeter regex... could be easier to lex using the standard way

    private var type = TokenType.PLAIN

    private var lastMatchIndex = 0

    private var currentPrefix = StringBuilder()

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

        lastMatchIndex = reader.index
        buffer.clear()

        this += type.wrap(data)
    }

    override fun tokenize(): List<Token> =
        buildList {
            reader.readWhileNotNull { char ->
                if (char == '*') {
                    when (type) {
                        TokenType.PLAIN -> {
                            push()
                            type = TokenType.EMPHASIS
                        }
                        TokenType.EMPHASIS -> {
                            if (buffer.length == 1) {
                                type = TokenType.STRONG
                            } else {
                                push()
                            }
                        }
                        TokenType.STRONG -> {
                            if (buffer.length == 2) {
                                type = TokenType.STRONG_EMPHASIS
                            } else {
                                push()
                            }
                        }
                        TokenType.STRONG_EMPHASIS -> push()
                    }
                }

                buffer.append(char)
            }

            push()
        }
}
