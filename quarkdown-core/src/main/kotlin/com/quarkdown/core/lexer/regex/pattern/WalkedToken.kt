package com.quarkdown.core.lexer.regex.pattern

import com.quarkdown.core.lexer.Token

/**
 * Result of a walker invocation during tokenization.
 * Produced by patterns that require secondary scanning beyond what regex can capture
 * (e.g., balanced delimiters in function call arguments).
 * @param token the token produced by the walker, with walker data embedded
 * @param charsConsumed number of additional characters consumed beyond the regex match
 */
data class WalkedToken(
    val token: Token,
    val charsConsumed: Int,
)
