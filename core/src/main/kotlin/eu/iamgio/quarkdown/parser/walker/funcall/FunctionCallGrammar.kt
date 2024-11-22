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
import eu.iamgio.quarkdown.function.call.FunctionCall
import eu.iamgio.quarkdown.parser.BlockTokenParser

/**
 * Grammar that defines and parses a function call.
 * The output object is [WalkedFunctionCall], which will later be converted to a [FunctionCall] by [BlockTokenParser].
 *
 * The following is an example of a function call:
 * ```
 * .func {arg1} {arg2} name:{arg3}
 *   body argument line 1
 *   body argument line 2
 * ```
 *
 * On its own, the function call grammar is context-aware, as the argument are not subject to syntactic rules,
 * as they may represent Markdown content (argument type checking is performed in the function expansion stage of the pipeline instead, see [eu.iamgio.quarkdown.function.call.binding]).
 * In order to achieve context-awareness, this grammar is stateful and mutable, as it needs to know whether it is currently parsing the outer 'rigid' syntax or an argument.
 *
 * @param allowsBody whether the function call allows an indented body argument.
 *                   Generally, this is true for block functions, false for inline functions.
 */
class FunctionCallGrammar(private val allowsBody: Boolean) : Grammar<WalkedFunctionCall>() {
    /**
     * Whether the parser is currently parsing an argument.
     * While parsing an argument, the parser should not perform syntactic checks as the argument may contain any content, including Markdown.
     * This is a mutable state variable that is set to true when an argument is being parsed and false when the argument ends.
     */
    private var inArg = false

    companion object {
        /**
         * The character that prefixes a function call.
         */
        const val BEGIN = "."

        /**
         * The pattern for an identifier (function name or argument name).
         * An identifier may also be a number: see implicit lambda arguments for example.
         */
        const val IDENTIFIER_PATTERN = "[a-zA-Z][a-zA-Z0-9]*|[0-9]+"

        /**
         * The character that begins an inline argument.
         */
        const val ARGUMENT_BEGIN = '{'

        /**
         * The character that ends an inline argument.
         */
        const val ARGUMENT_END = '}'
    }

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

    /**
     * Token that matches the content of an inline (= non-body) argument.
     * This token has the highest priority in the grammar: if [inArg] is `true`,
     * this is the only viable token to match and other syntax rules are ignored.
     */
    private val argContent by token { string, position ->
        if (!inArg) return@token 0

        var depth = 0
        for (x in position until string.length) {
            when {
                // Unescaped argument begin character {
                unescapedMatch(string, x, ARGUMENT_BEGIN) != 0 -> depth++
                // Unescaped argument end character }
                // This leads to the end of the argument if the delimiters are balanced.
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

    /**
     * Token that matches the beginning of a function call.
     */
    private val begin by literalToken(BEGIN)

    /**
     * Token that matches whitespace, ignored between arguments
     */
    private val whitespace by regexToken("[ \\t]+")

    /**
     * Token that matches the beginning of an inline argument.
     * Sets [inArg] to `true` if found.
     * @see ARGUMENT_BEGIN
     */
    private val argumentBegin by token { string, position ->
        unescapedMatch(string, position, ARGUMENT_BEGIN) {
            inArg = true
        }
    }

    /**
     * Token that matches the end of an inline argument.
     * Sets [inArg] to `false` if found.
     * @see ARGUMENT_END
     */
    private val argumentEnd by token { string, position ->
        unescapedMatch(string, position, ARGUMENT_END) {
            inArg = false
        }
    }

    /**
     * Token that matches the delimiter between an argument name and its value.
     * e.g. `name:{value}`.
     */
    private val argumentNameDelimiter by literalToken(":")

    /**
     * Token that matches an identifier (function name or argument name).
     * @see IDENTIFIER_PATTERN
     */
    private val identifier by token { string, position ->
        if (inArg) return@token 0
        regexToken(IDENTIFIER_PATTERN).match(string, position)
    }

    /**
     * Token that matches the content of a body argument.
     * A body argument is not wrapped in braces and must be consistently indented with at least two spaces or one tab.
     */
    private val bodyArgContent by token { string, position ->
        if (!allowsBody || inArg) return@token 0

        // Length of the body argument.
        var length = 0
        // Whether at least one indented line has been found.
        var found = false

        for (line in string.substring(position).lineSequence()) {
            val hasIndent = line.startsWith("  ") || line.startsWith("\t")

            // Blank lines (even if not indented) are included in the body argument.
            // In order to be matched, however, the body argument must contain at least one non-blank indented line.

            if (line.isNotBlank()) {
                when {
                    hasIndent -> found = true
                    else -> break
                }
            }

            length += line.length
            if (string.getOrNull(length + position) == '\n') length++ // Include line break in the character count.
        }

        when {
            found -> length
            else -> 0
        }
    }

    /**
     * Parses an inline argument.
     * An inline argument is wrapped in braces and may contain any kind of content.
     * @see argContent
     */
    private val argumentParser =
        (
            -optional(whitespace) and
                // Optional named argument.
                optional(identifier and -argumentNameDelimiter) and
                -argumentBegin and
                // Argument content.
                optional(argContent map { it.text.trimIndent().trim() }) and
                -argumentEnd
        ) map { (name, value) -> WalkedArgument(name?.text, value ?: "", isBody = false) }

    /**
     * Parses a body argument.
     * @see bodyArgContent
     */
    private val bodyArgumentParser =
        bodyArgContent map { value ->
            value.text.takeUnless { it.isBlank() }?.let {
                WalkedArgument(null, it.trimIndent().trimEnd(), isBody = true)
            }
        }

    /**
     * Entry point. Parses the whole function call.
     * A function call consists of a function name, inline arguments and an optional body argument.
     */
    override val rootParser =
        (
            // Function name.
            -begin and identifier and
                // Inline arguments.
                zeroOrMore(argumentParser) and
                // Body argument.
                optional(-optional(whitespace) and bodyArgumentParser)
        ) map { (id, args, body) ->
            WalkedFunctionCall(
                id.text,
                args,
                body,
            )
        }
}
