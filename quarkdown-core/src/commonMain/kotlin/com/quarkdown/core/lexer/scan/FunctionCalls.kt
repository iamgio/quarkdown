package com.quarkdown.core.lexer.scan

import com.github.h0tk3y.betterParse.parser.ParseException
import com.quarkdown.core.parser.walker.WalkerParsingResult
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar
import com.quarkdown.core.parser.walker.funcall.FunctionCallWalkerParser
import com.quarkdown.core.parser.walker.funcall.WalkedFunctionCall

/**
 * What may precede a function call, as an alternation: the start of a line, whitespace, or a character that
 * is not a letter, a digit, a dot or a backslash.
 *
 * The lexers ask [functionCallStartAt]; this spelling exists for the language server, which scans partial
 * documents of its own, so that the rule is stated once.
 */
const val FUNCTION_CALL_PATTERN_BEFORE = "^|\\s|[^a-zA-Z0-9.\\\\]"

/**
 * Detects where a call may begin, without consuming anything. A call begins either at its own `.` followed by
 * an identifier character, or at the `{` of a wrapped call.
 * @param source text to inspect
 * @param index index to test
 * @return whether a function call may begin at [index]
 */
fun functionCallStartAt(
    source: CharSequence,
    index: Int,
): Boolean {
    if (source.getOrNull(index) == FunctionCallGrammar.ARGUMENT_BEGIN) return true
    if (source.getOrNull(index) != FunctionCallGrammar.BEGIN) return false
    return source.getOrNull(index + 1)?.isFunctionCallIdentifier() == true
}

/**
 * At least as wide as the first character [FunctionCallGrammar.IDENTIFIER_PATTERN] accepts: a name the
 * grammar rejects costs one failed walk, while a name this check rejects never reaches the grammar at all.
 * @return whether the character may start a function call's identifier
 */
private fun Char.isFunctionCallIdentifier(): Boolean = isLetterOrDigit() || this == '_'

/**
 * The same rule as [FUNCTION_CALL_PATTERN_BEFORE], as a predicate: the two must agree, since the compiler
 * lexes with this one and the language server matches with that one.
 * @param source text to inspect
 * @param index index the call would start at
 * @return whether a call may start there given what precedes it
 */
fun isFunctionCallAllowedBefore(
    source: CharSequence,
    index: Int,
): Boolean {
    val before = source.getOrNull(index - 1) ?: return true
    if (before.isWhitespace()) return true
    return !(CharCategories.isAsciiLetterOrDigit(before) || before == FunctionCallGrammar.BEGIN || before == ESCAPE)
}

/**
 * Walks the function call that starts at [from], body argument included.
 * @param source text to walk
 * @param from index the call starts at
 * @return the walker's result, or `null` when no call parses there
 */
internal fun walkFunctionCallAt(
    source: CharSequence,
    from: Int,
): WalkerParsingResult<WalkedFunctionCall>? =
    try {
        FunctionCallWalkerParser(source.walkerSlice(from), allowsBody = true).parse()
    } catch (_: ParseException) {
        null
    }

/**
 * Slices the source a function call walker reads: from [from] to the end of the source, the scan's own
 * trailing newline excluded, so a body argument that reaches the end of the document does not gain a line.
 * @param from index the walker starts at
 * @return the slice to walk
 */
internal fun CharSequence.walkerSlice(from: Int): CharSequence {
    val end = if (endsWith('\n')) length - 1 else length
    return subSequence(from, maxOf(from, end))
}
