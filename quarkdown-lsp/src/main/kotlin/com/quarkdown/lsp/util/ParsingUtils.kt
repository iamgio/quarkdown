package com.quarkdown.lsp.util

import com.quarkdown.core.util.substringWithinBounds
import com.quarkdown.lsp.tokenizer.FunctionCall
import com.quarkdown.lsp.tokenizer.FunctionCallToken

/**
 * Returns the remainder of the parsing result, truncated to the specified index relative to the original source text.
 *
 * For example, if the source is `"Hello, world!"` and the parsing completes with `"Hello"`,
 * the regular remainder would be `", world!"`.
 * If this function is called with an index of `10`, it will return `", wor"`.
 * @param index the index to which the remainder should be returned
 * @return the substring from the start of the remainder to the specified index
 */
fun FunctionCall.remainderUntilIndex(index: Int): String? =
    parserResult.remainder
        .substringWithinBounds(0, index - range.endInclusive)

/**
 * Given a function call that may contain chained calls, e.g. `.func1 param:{value1}::func2 param:{value2}`,
 * which would be normally tokenized as a flat list of tokens,
 * this property returns a sequence of (function name, tokens) pairs for each function in the chain.
 *
 * Note that tokens that occur before any function name, such as `.`, are ignored.
 */
val FunctionCall.tokensByChainedCall: Sequence<Pair<String, List<FunctionCallToken>>>
    get() =
        sequence {
            var currentFunctionName: String? = null
            var currentTokens = mutableListOf<FunctionCallToken>()

            tokens.forEach { token ->
                if (token.type == FunctionCallToken.Type.FUNCTION_NAME) {
                    currentFunctionName?.let { yield(it to currentTokens.toList()) }
                    currentFunctionName = token.lexeme
                    currentTokens = mutableListOf()
                } else {
                    currentFunctionName?.let { currentTokens.add(token) }
                }
            }
            currentFunctionName?.let { yield(it to currentTokens.toList()) }
        }
