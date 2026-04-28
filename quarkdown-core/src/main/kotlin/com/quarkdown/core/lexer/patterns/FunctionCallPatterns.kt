package com.quarkdown.core.lexer.patterns

import com.quarkdown.core.lexer.regex.RegexBuilder
import com.quarkdown.core.lexer.regex.pattern.TokenRegexPattern
import com.quarkdown.core.lexer.regex.pattern.WalkedToken
import com.quarkdown.core.lexer.tokens.FunctionCallToken
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar
import com.quarkdown.core.parser.walker.funcall.FunctionCallWalkerParser

/**
 * Patterns for block and inline function calls.
 */
class FunctionCallPatterns {
    /**
     * Function name prefixed by '.', followed by a sequence of arguments.
     * Can be preceded by the beginning of the line, a whitespace or a symbol.
     * This is a 'flag' pattern, meaning it does not capture any content,
     * but instead detects the beginning of a function call and delegates the scanning to [FunctionCallWalkerParser].
     */
    val inlineFunctionCall by lazy {
        TokenRegexPattern(
            name = "InlineFunctionCall",
            wrap = { error("Inline function call tokens are constructed by the walker") },
            // The name of the function prefixed by a dot.
            regex =
                RegexBuilder("(?:(?<=before)(?=call))|(?:(?=wrapcall))")
                    .withReference("before", FUNCTION_CALL_PATTERN_BEFORE)
                    .withReference("call", "begin(name)")
                    .withReference("wrap", Regex.escape(FunctionCallGrammar.ARGUMENT_BEGIN.toString()))
                    .withReference("begin", Regex.escape(FunctionCallGrammar.BEGIN.toString()))
                    .withReference("name", FunctionCallGrammar.IDENTIFIER_PATTERN)
                    .build(),
            walker = { data, remaining ->
                val result = FunctionCallWalkerParser(remaining, allowsBody = false).parse()
                WalkedToken(
                    token = FunctionCallToken(data, isBlock = false, walkerResult = result),
                    charsConsumed = result.endIndex,
                )
            },
        )
    }

    /**
     * An isolated function call.
     * Function name prefixed by '.', followed by a sequence of arguments
     * and an optional body, indented by at least 2 spaces or 1 tab like a list item body.
     * This is a 'flag' pattern, meaning it does not capture any content,
     * but instead detects the beginning of a function call and delegates the scanning to [FunctionCallWalkerParser].
     *
     * The walker determines whether the call is block-level by checking the remainder after parsing:
     * if non-whitespace content follows on the same line, the call is inline-level and the walker rejects
     * the match, allowing the paragraph pattern to capture the entire line instead.
     */
    val blockFunctionCall by lazy {
        TokenRegexPattern(
            name = "FunctionCall",
            wrap = { error("Block function call tokens are constructed by the walker") },
            regex =
                RegexBuilder("^ {0,3}(?:call))")
                    .withReference("call", inlineFunctionCall.regex.dropLast(1))
                    .build(),
            walker = { data, remaining ->
                val result = FunctionCallWalkerParser(remaining, allowsBody = true).parse()
                // The function call is block-level only if it spans the entire header line.
                val isBlock = remaining.isBlankAfterFunctionCall(result.endIndex)
                if (!isBlock) return@TokenRegexPattern null

                WalkedToken(
                    token = FunctionCallToken(data, isBlock = true, walkerResult = result),
                    charsConsumed = result.endIndex,
                )
            },
        )
    }

    /**
     * Block function call variant for use in expression evaluation.
     * Uses the same regex as [blockFunctionCall] (with `allowsBody = true`),
     * but always produces a token: expression evaluation does not distinguish between block and inline
     * function calls, so no rejection is needed.
     */
    val expressionBlockFunctionCall by lazy {
        TokenRegexPattern(
            name = "FunctionCall",
            wrap = { error("Block function call tokens are constructed by the walker") },
            regex = blockFunctionCall.regex,
            walker = { data, remaining ->
                val result = FunctionCallWalkerParser(remaining, allowsBody = true).parse()
                WalkedToken(
                    token = FunctionCallToken(data, isBlock = true, walkerResult = result),
                    charsConsumed = result.endIndex,
                )
            },
        )
    }
}

/**
 * Accepted pattern before a function call.
 */
const val FUNCTION_CALL_PATTERN_BEFORE = "^|\\s|[^a-zA-Z0-9.\\\\]"

/**
 * Whether a function call that was parsed from [this] source ends cleanly, with no trailing content
 * on the same line as the call's last argument. This determines whether the call is block-level.
 *
 * @param endIndex the index where the walker stopped parsing
 * @return `true` if the call is block-level (no trailing content on the last line)
 */
private fun CharSequence.isBlankAfterFunctionCall(endIndex: Int): Boolean {
    // If the walker stopped at a line boundary (e.g. after body arguments), the call's content ended cleanly.
    if (endIndex >= length || this[endIndex - 1] == '\n') return true
    // Otherwise, check if the rest of the line after endIndex is blank.
    val lineEnd = indexOf('\n', endIndex).let { if (it < 0) length else it }
    return substring(endIndex, lineEnd).isBlank()
}
