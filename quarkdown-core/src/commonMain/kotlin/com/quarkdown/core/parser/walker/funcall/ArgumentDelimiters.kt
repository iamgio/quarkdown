package com.quarkdown.core.parser.walker.funcall

import com.quarkdown.core.lexer.scan.Matchers.balancedDelimiters
import com.quarkdown.core.lexer.scan.Matchers.unescapedMatch
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar.Companion.ARGUMENT_BEGIN
import com.quarkdown.core.parser.walker.funcall.FunctionCallGrammar.Companion.ARGUMENT_END

/**
 * A variant of the delimiters that wrap an inline function call argument.
 * Every variant begins with [ARGUMENT_BEGIN] and ends with [ARGUMENT_END].
 * @param begin the opening delimiter
 * @param end the closing delimiter
 * @param preservesWhitespace whether the leading and trailing whitespace of the content is preserved or trimmed
 * @see FunctionCallGrammar
 */
enum class ArgumentDelimiters(
    val begin: String,
    val end: String,
    val preservesWhitespace: Boolean,
) {
    /**
     * Quoted argument, which preserves whitespace: `{"content"} != {" content "}`.
     */
    QUOTED("$ARGUMENT_BEGIN\"", "\"$ARGUMENT_END", preservesWhitespace = true),

    /**
     * Regular argument: `{content} == { content }`.
     */
    PLAIN("$ARGUMENT_BEGIN", "$ARGUMENT_END", preservesWhitespace = false),
    ;

    /**
     * Measures the content of an argument wrapped by this variant.
     * @param string the source to scan
     * @param contentStart the index right after [begin]
     * @return the length of the content
     */
    fun contentLength(
        string: CharSequence,
        contentStart: Int,
    ): Int = balancedDelimiters(string, contentStart, ARGUMENT_BEGIN, ARGUMENT_END) - end.length + 1

    /**
     * @param string the source to scan
     * @param position the index of the opening delimiter
     * @return whether this variant wraps the argument whose opening delimiter is at [position]
     */
    private fun wrapsAt(
        string: CharSequence,
        position: Int,
    ): Boolean {
        if (!string.startsWith(begin, position)) return false

        val contentStart = position + begin.length
        val length = contentLength(string, contentStart)
        if (length < 0) return false

        val endStart = contentStart + length
        return unescapedMatch(string, endStart, end) != 0
    }

    /**
     * @param content the raw content of an argument wrapped by this variant
     * @return the value of the argument: [content] as-is if this variant [preservesWhitespace], trimmed otherwise
     */
    fun valueOf(content: String): String =
        when {
            preservesWhitespace -> content
            else -> content.trimIndent().trim()
        }

    companion object {
        /**
         * @param string the source to scan
         * @param position the index of an unescaped [ARGUMENT_BEGIN]
         * @return the most specific variant that wraps the argument at [position], falling back to [PLAIN]
         */
        fun at(
            string: CharSequence,
            position: Int,
        ): ArgumentDelimiters = entries.firstOrNull { it.wrapsAt(string, position) } ?: PLAIN

        /**
         * @param begin the opening delimiter of an argument, as matched by [at]
         * @return the variant whose opening delimiter is [begin]
         */
        fun ofBegin(begin: String): ArgumentDelimiters = entries.first { it.begin == begin }
    }
}
