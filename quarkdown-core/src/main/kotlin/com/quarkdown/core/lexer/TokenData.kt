package com.quarkdown.core.lexer

import com.quarkdown.core.parser.walker.WalkerParsingResult

/**
 * Data of a single, usually small, substring of the source code that stores a chunk of information.
 * For instance, the Markdown code `Hello _Quarkdown_` contains the tokens `Hello `, `_`, `Quarkdown`, `_`.
 * @param text the substring extracted from the source code, also known as _lexeme_.
 * @param position location of the token within the source code
 * @param groups capture groups values for this token
 * @param namedGroups capture groups that hold a name. [groups] does not contain groups from [namedGroups]
 * @see Token
 */
data class TokenData(
    val text: String,
    val position: IntRange,
    val groups: Sequence<String> = emptySequence(),
    val namedGroups: Map<String, String> = emptyMap(),
    val walkerResult: WalkerParsingResult<*>? = null,
)
