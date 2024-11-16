package eu.iamgio.quarkdown.parser.walker.funcall

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.lexer.token

/**
 *
 */
private const val ARGUMENT_BEGIN = '{'
private const val ARGUMENT_END = '}'

class FunctionCallGrammar(private val allowsBody: Boolean) : Grammar<WalkedFunctionCall>() {
    private var inArg = false

    /**
     * Matches a character if it is not escaped.
     * @param string the string to match
     * @param position the position of the character to match
     * @param char the character to match
     * @param onMatch optional action to perform if the character is matched
     * @return 1 if the character is matched and not preceded by an escape character, 0 otherwise
     */
    private fun unescapedMatch(
        string: CharSequence,
        position: Int,
        char: Char,
        onMatch: () -> Unit = {},
    ): Int =
        when {
            string[position] != char -> 0
            string.getOrNull(position - 1) != '\\' -> {
                onMatch()
                1
            }

            else -> 0
        }

    private val argContent by token { string, position ->
        if (!inArg) return@token 0

        var depth = 0
        for (x in position until string.length) {
            when {
                // Unescaped argument begin character {
                unescapedMatch(string, x, ARGUMENT_BEGIN) != 0 -> depth++
                // Unescaped argument end character }
                unescapedMatch(string, x, ARGUMENT_END) != 0 -> {
                    if (depth == 0) {
                        inArg = false
                        return@token x - position
                    }
                    depth--
                }
            }
        }
        0
    }

    private val begin by literalToken(".")

    private val whitespace by regexToken("[ \\t]+")

    private val argumentBegin by token { string, position ->
        unescapedMatch(string, position, ARGUMENT_BEGIN) {
            inArg = true
        }
    }

    private val argumentEnd by token { string, position ->
        unescapedMatch(string, position, ARGUMENT_END) {
            inArg = false
        }
    }

    private val argumentNameDelimiter by literalToken(":")

    private val identifier by token { string, position ->
        if (inArg) return@token 0
        regexToken("([a-zA-Z][a-zA-Z0-9]*)|[0-9]+").match(string, position)
    }

    private val bodyArgContent by token { string, position ->
        if (!allowsBody || inArg) return@token 0

        var index = position

        for (line in string.substring(position).lineSequence()) {
            val hasIndent = line.startsWith("  ") || line.startsWith("\t")
            if (!hasIndent && line.isNotBlank()) break

            index += line.length + 1
        }

        when {
            index == position -> 0
            else -> index - position - 1 // Strip the last newline character
        }
    }

    private val argumentParser =
        (
            -optional(whitespace) and
                optional(identifier and -argumentNameDelimiter) and
                -argumentBegin and
                optional(argContent map { it.text.trimIndent().trim() }) and
                -argumentEnd
        ) map { (name, value) -> WalkedArgument(name?.text, value ?: "", isBody = false) }

    // A body argument is not wrapped in braces and must be consistently indented
    private val bodyArgumentParser =
        bodyArgContent map { value ->
            value.text.takeUnless { it.isBlank() }?.let {
                WalkedArgument(null, it.trimIndent(), isBody = true)
            }
        }

    override val rootParser =
        (
            -begin and identifier and zeroOrMore(argumentParser) and optional(-optional(whitespace) and bodyArgumentParser)
        ) map { (id, args, body) -> WalkedFunctionCall(id.text, args, body) }
}
