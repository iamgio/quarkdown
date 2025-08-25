package com.quarkdown.core.parser.walker.funcall

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.combinators.zeroOrMore
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.lexer.token
import com.quarkdown.core.function.call.FunctionCall
import com.quarkdown.core.parser.BlockTokenParser
import com.quarkdown.core.parser.walker.GrammarUtils.balancedDelimitersMatch
import com.quarkdown.core.parser.walker.GrammarUtils.unescapedMatch
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar.Companion.ARGUMENT_BEGIN
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar.Companion.ARGUMENT_END
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar.Companion.IDENTIFIER_PATTERN

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
 * as they may represent Markdown content (argument type checking is performed in the function expansion stage of the pipeline instead, see [com.quarkdown.core.function.call.binding]).
 * In order to achieve context-awareness, this grammar is stateful and mutable, as it needs to know whether it is currently parsing the outer 'rigid' syntax or an argument.
 *
 * @param allowsBody whether the function call allows an indented body argument.
 *                   Generally, this is true for block functions, false for inline functions.
 */
class FunctionCallGrammar(
    private val allowsBody: Boolean,
) : Grammar<WalkedFunctionCall>() {
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

        /**
         * The character that delimits a named argument.
         */
        const val NAMED_ARGUMENT_DELIMITER = ":"

        /**
         * The character that separates chained function calls.
         */
        const val CHAIN_SEPARATOR = "::"
    }

    /**
     * Token that matches the content of an inline (= non-body) argument.
     * This token has the highest priority in the grammar: if [inArg] is `true`,
     * this is the only viable token to match and other syntax rules are ignored.
     */
    private val argContent by token { string, position ->
        if (!inArg) return@token 0

        val length =
            balancedDelimitersMatch(
                string,
                position,
                ARGUMENT_BEGIN,
                ARGUMENT_END,
            )

        if (length > 0) {
            inArg = false
        }

        length
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
     * Token that matches the separator between chained function calls.
     * e.g. `foo::bar`.
     */
    private val chainSeparator by literalToken(CHAIN_SEPARATOR)

    /**
     * Token that matches the delimiter between an argument name and its value.
     * e.g. `name:{value}`.
     */
    private val argumentNameDelimiter by literalToken(NAMED_ARGUMENT_DELIMITER)

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
                argumentBegin and
                // Argument content.
                optional(argContent) and
                argumentEnd
        ) map { (name, begin, value, end) ->
            WalkedFunctionArgument(
                name = name?.text,
                value = value?.text?.trimIndent()?.trim() ?: "",
                range = begin.offset until (end.offset + end.length),
            )
        }

    /**
     * Parses a body argument.
     * @see bodyArgContent
     */
    private val bodyArgumentParser =
        bodyArgContent map { value ->
            value.text.takeUnless { it.isBlank() }?.let {
                WalkedFunctionArgument(
                    name = null,
                    value = it.trimIndent().trimEnd(),
                    range = value.offset until (value.offset + value.length),
                )
            }
        }

    /**
     * Parses a single function call.
     * A function call consists of a function name, inline arguments and an optional body argument.
     */
    private val callParser =
        (
            // Function name.
            identifier and
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

    /**
     * Entry point of the grammar.
     * Parses the whole chain of function calls.
     * The result is an ordered linked list of [WalkedFunctionCall]s, and the first of them is returned.
     */
    override val rootParser =
        -begin and callParser and zeroOrMore(-chainSeparator and callParser) map { (first, rest) ->
            var current = first
            for (next in rest) {
                current.next = next
                current = next
            }
            first
        }
}
