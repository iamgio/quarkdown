package com.quarkdown.lsp.tokenizer

/**
 * Finds the last token of a specific type before a given index in the list of function call tokens.
 * If a reset token is encountered, it discards the match.
 *
 * Example (let `|` be the cursor position in the text):
 * ```
 * .function param1:{arg1} param2:{ar|g2}
 * ```
 *
 * Calling this function with:
 * - the position of `|` (the cursor) as the index;
 * - [FunctionCallToken.Type.PARAMETER_NAME] as the match type;
 * - [FunctionCallToken.Type.INLINE_ARGUMENT_END] as the reset type
 * will return the token for `param2` (the last parameter name before the cursor).
 *
 * @param beforeIndex The index before which to search for the matching token.
 * @param matchType The type of token to match.
 * @param reset A set of token types that will reset the match if encountered.
 * @return The last matching token before the specified index, or null if no match is found.
 */
fun List<FunctionCallToken>.findMatchingTokenBeforeIndex(
    beforeIndex: Int,
    matchType: FunctionCallToken.Type,
    reset: Set<FunctionCallToken.Type>,
): FunctionCallToken? {
    var match: FunctionCallToken? = null

    for (token in this) {
        if (beforeIndex in token.range) {
            return match
        }
        if (token.type == matchType) {
            match = token
        } else if (token.type in reset) {
            match = null
        }
    }
    return match
}
