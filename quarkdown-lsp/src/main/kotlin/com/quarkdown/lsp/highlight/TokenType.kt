package com.quarkdown.lsp.highlight

/**
 * Types of tokens used for semantic highlighting.
 * @param legendName the name of the token type, used in the semantic token legend
 */
enum class TokenType(
    val legendName: String,
) {
    /**
     * A function call identifier.
     *
     * ```
     * .function
     * ```
     */
    FUNCTION_CALL("function"),
    ;

    val index: Int
        get() = ordinal

    companion object {
        val legend: List<String> = entries.map { it.legendName }
    }
}
