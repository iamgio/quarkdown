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
     *  ^^^^^^^^
     * ```
     */
    FUNCTION_CALL_IDENTIFIER("function"),

    /**
     * A named parameter in a function call.
     *
     * ```
     * .function parameter:{...}
     *           ^^^^^^^^^^
     * ```
     */
    FUNCTION_CALL_NAMED_PARAMETER("typeParameter"),
    ;

    /**
     * The index of this token type in the semantic token legend.
     * This is used to encode the token type.
     */
    val index: Int
        get() = ordinal

    companion object {
        /**
         * The semantic token legend, which maps token types to their names.
         * This is used in the LSP to provide a legend for semantic tokens
         * which can then be referenced via their [index].
         */
        val legend: List<String> = entries.map { it.legendName }
    }
}
