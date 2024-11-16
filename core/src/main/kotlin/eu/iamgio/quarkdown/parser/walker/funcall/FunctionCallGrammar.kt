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

    private val begin by literalToken(".")

    private val whitespace by regexToken("[ \\t]+")

    private val argumentBegin by token { string, position ->
        if (string[position] == ARGUMENT_BEGIN) {
            inArg = true
            1
        } else {
            0
        }
    }

    private val argumentEnd by token { string, position ->
        if (string[position] == ARGUMENT_END) {
            inArg = false
            1
        } else {
            0
        }
    }

    private val argumentNameDelimiter by literalToken(":")

    private val identifier by token { string, position ->
        if (inArg) return@token 0
        regexToken("[a-zA-Z]+|[0-9]+").match(string, position)
    }

    private val argContent by token { string, position ->
        if (!inArg) return@token 0

        var depth = 0
        for (x in position until string.length) {
            when (string[x]) {
                ARGUMENT_BEGIN -> depth++
                ARGUMENT_END -> {
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

    private val bodyArgContent by token { string, position ->
        if (!allowsBody || inArg) return@token 0

        var index = position
        var foundIndent = false

        string.substring(position).lineSequence()
            .filter {
                val hasIndent = it.startsWith("  ") || it.startsWith("\t")
                if (hasIndent) foundIndent = true

                hasIndent || (foundIndent && it.isBlank())
            }
            .forEach { index += it.length + 1 }

        index - position
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
        bodyArgContent map { value -> WalkedArgument(null, value.text.trimIndent(), isBody = true) }

    override val rootParser =
        (
            -begin and identifier and zeroOrMore(argumentParser) // and optional(bodyArgumentParser)
        ) map { (id, args/*, body*/) -> WalkedFunctionCall(id.text, args, null/*body*/) }
}
