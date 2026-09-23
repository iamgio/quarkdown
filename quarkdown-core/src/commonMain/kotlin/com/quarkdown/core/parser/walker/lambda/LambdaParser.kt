package com.quarkdown.core.parser.walker.lambda

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException

/**
 * Parses a raw lambda string into a [ParsedLambda].
 *
 * If the input matches the lambda grammar (`param1 param2?: body`),
 * the parameters and body are extracted accordingly.
 * Otherwise, the entire input is treated as the body with no parameters.
 *
 * @see LambdaGrammar
 */
object LambdaParser {
    /**
     * @param raw raw lambda string to parse
     * @return parsed lambda with parameters and body
     */
    fun parse(raw: String): ParsedLambda =
        try {
            LambdaGrammar().parseToEnd(raw)
        } catch (_: ParseException) {
            // No valid header: the entire input is the body, with no parameters.
            ParsedLambda(parameters = emptyList(), body = raw)
        }
}
