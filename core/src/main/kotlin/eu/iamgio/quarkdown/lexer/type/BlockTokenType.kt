package eu.iamgio.quarkdown.lexer.type

/**
 * Token types that represent macro-blocks.
 */
enum class BlockTokenType : TokenType {
    /**
     * A new line.
     */
    NEWLINE,

    /**
     * A code block defined via indentation.
     * Example:
     * ```
     *     Code
     * ```
     */
    BLOCK_CODE,

    /**
     * A code block defined via fences.
     * Example:
     * ~~~
     * ```lang
     * Code
     * ```
     * ~~~
     */
    FENCES_CODE,

    /**
     * A horizontal line.
     * Example:
     * ```
     * ---
     * ```
     */
    HORIZONTAL_RULE,

    /**
     * A heading defined via prefix symbols.
     * Example:
     * ```
     * # Heading
     * ```
     */
    HEADING,

    /**
     * A heading defined via newline symbols.
     * Example:
     * ```
     * Heading
     * =======
     * ```
     */
    SETEXT_HEADING,

    /**
     * Empty lines.
     */
    BLOCK_TEXT,

    /**
     * Creation of a link reference.
     * Example:
     * ```
     * [label]: url "Title"
     * ```
     */
    LINK_DEFINITION,

    /**
     * A list, either ordered or unordered.
     * Examples:
     * ```
     * - A
     * - B
     *
     * 1. First
     * 2. Second
     * ```
     */
    LIST,

    /**
     * An HTML block.
     * Example:
     * ```
     * <p>
     *     Code
     * </p>
     * ```
     */
    HTML,

    /**
     * A text paragraph.
     */
    PARAGRAPH,

    /**
     * A block quote.
     * Example:
     * ```
     * > Quote
     * ```
     */
    BLOCKQUOTE
}
