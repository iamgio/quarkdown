package com.quarkdown.core.parser.walker.lambda

import com.github.h0tk3y.betterParse.combinators.and
import com.github.h0tk3y.betterParse.combinators.map
import com.github.h0tk3y.betterParse.combinators.oneOrMore
import com.github.h0tk3y.betterParse.combinators.optional
import com.github.h0tk3y.betterParse.combinators.unaryMinus
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.lexer.token
import com.quarkdown.core.function.value.data.LambdaParameter
import com.quarkdown.core.parser.walker.GrammarUtils.unescapedMatch

/**
 * Parsed result of a lambda expression.
 * @param parameters named parameters of the lambda, empty if no header is present
 * @param body the body content of the lambda
 */
data class ParsedLambda(
    val parameters: List<LambdaParameter>,
    val body: String,
)

/**
 * Grammar that parses a lambda expression.
 *
 * A lambda has the form `param1 param2?: body content`,
 * where the header (`param1 param2?:`) defines parameters and the body is everything after the delimiter.
 *
 * Parameters are word-character identifiers, optionally suffixed with `?` to mark them as optional.
 * The delimiter is a colon (`:`).
 *
 * If the header cannot be parsed (i.e. no valid parameter-delimiter sequence is found),
 * the entire input is treated as a body with no parameters.
 *
 * The grammar is context-aware: while parsing the header, normal tokens (parameter names, whitespace, etc.)
 * are matched individually. Once the delimiter is matched, a stateful [body] token activates
 * and captures the entire remaining input in one shot.
 *
 * @see ParsedLambda
 */
class LambdaGrammar : Grammar<ParsedLambda>() {
    private companion object {
        /**
         * Suffix character that marks a lambda parameter as optional.
         */
        const val OPTIONAL_SUFFIX = '?'

        /**
         * The character that separates the parameter header from the body.
         */
        const val DELIMITER = ':'

        /**
         * Pattern for a parameter name: one or more word characters.
         */
        const val PARAMETER_NAME_PATTERN = "\\w+"
    }

    /**
     * Whether the parser is still in the header (parameter list).
     * Once the delimiter is matched, this is set to `false` so that the [body] token captures everything remaining.
     */
    private var inHeader = true

    /**
     * Captures the entire remaining input as the lambda body.
     * Only active after the delimiter has been matched (i.e. when [inHeader] is `false`).
     */
    private val body by token { string, position ->
        if (inHeader) 0 else string.length - position
    }

    /**
     * Suffix that marks a lambda parameter as optional.
     */
    private val optionalSuffix by literalToken(OPTIONAL_SUFFIX.toString())

    /**
     * Whitespace between parameters, ignored.
     */
    private val whitespace by regexToken("[ \\t]+")

    /**
     * A parameter name: one or more word characters.
     */
    private val parameterName by regexToken(PARAMETER_NAME_PATTERN)

    /**
     * The delimiter that separates the header from the body.
     * Transitions the grammar out of header mode so that [body] activates.
     * An escaped delimiter (`\:`) is not matched.
     */
    private val delimiter by token { string, position ->
        unescapedMatch(string, position, DELIMITER) {
            inHeader = false
        }
    }

    /**
     * Parses a single parameter: a name optionally followed by `?`.
     */
    private val parameterParser =
        (parameterName and optional(optionalSuffix)) map { (name, optional) ->
            LambdaParameter(name.text, isOptional = optional != null)
        }

    /**
     * Entry point: one or more whitespace-separated parameters, a delimiter, and the remaining body.
     */
    override val rootParser =
        (
            oneOrMore(
                -optional(whitespace) and parameterParser,
            ) and -delimiter and optional(body)
        ) map { (params, body) ->
            ParsedLambda(params, body?.text?.trimStart().orEmpty())
        }
}
