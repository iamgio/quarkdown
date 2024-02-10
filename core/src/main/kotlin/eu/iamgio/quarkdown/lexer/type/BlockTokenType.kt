package eu.iamgio.quarkdown.lexer.type

/**
 * Token types that represent macro-blocks.
 */
enum class BlockTokenType : TokenType {
    NEWLINE,
    BLOCK_CODE,
    FENCES_CODE,
    HORIZONTAL_RULE,
    HEADING,
    SETEXT_HEADING,
    BLOCK_TEXT,
    LINK_DEFINITION,
    LIST,
    HTML,
    PARAGRAPH,
    BLOCKQUOTE
}
