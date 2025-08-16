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
    FUNCTION_CALL_NAMED_PARAMETER("parameter"),

    /**
     * Delimiters of an inline argument in a function call.
     *
     * ```
     * .function {...}
     *           ^   ^
     * ```
     */
    FUNCTION_CALL_INLINE_ARGUMENT_DELIMITER("keyword"),

    /**
     * A number value.
     *
     * ```
     * 20
     * ```
     */
    NUMBER("number"),

    /**
     * A range value.
     *
     * ```
     * 10..20
     * ```
     *
     * @see com.quarkdown.core.function.value.data.Range
     */
    RANGE("number"),

    /**
     * A boolean value.
     *
     * ```
     * yes/true/no/false
     * ```
     */
    BOOLEAN("keyword"),

    /**
     * A size value or a list of sizes.
     *
     * ```
     * 10px 20em 5px 2in
     * ```
     *
     * @see com.quarkdown.core.document.size.Sizes
     */
    SIZE("property"),

    /**
     * An enum value.
     *
     * ```
     * spacebetween
     * ```
     */
    ENUM("enum"),
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
