package com.quarkdown.core.util

import org.apache.commons.text.StringEscapeUtils

/**
 * Represents a target (commonly a language or format) that strings can be escaped for.
 *
 * For instance:
 * - in HTML `<` becomes `&lt;`
 * - in JavaScript `"` becomes `\"`
 *
 * This is the inverse of [UnescapeTarget].
 */
sealed interface EscapeTarget {
    /**
     * Escapes the input string for the target format.
     * @param input the string to escape
     * @return the escaped string
     */
    fun escape(input: String): String
}

/**
 * Represents a target (commonly a language or format) that strings can be unescaped from.
 *
 * For instance:
 * - in HTML `&lt;` becomes `<`
 * - in JavaScript `\"` becomes `"`
 *
 * This is the inverse of [EscapeTarget].
 */
sealed interface UnescapeTarget {
    /**
     * Unescapes the input string from the target format.
     * @param input the string to unescape
     * @return the unescaped string
     */
    fun unescape(input: String): String
}

/**
 * Utilities for escaping and unescaping strings for various targets.
 */
object Escape {
    object Html : EscapeTarget, UnescapeTarget {
        override fun escape(input: String): String = StringEscapeUtils.escapeHtml4(input)

        override fun unescape(input: String): String = StringEscapeUtils.unescapeHtml4(input)
    }

    object JavaScript : EscapeTarget, UnescapeTarget {
        override fun escape(input: String): String = StringEscapeUtils.escapeEcmaScript(input)

        override fun unescape(input: String): String = StringEscapeUtils.unescapeEcmaScript(input)
    }

    object Json : EscapeTarget, UnescapeTarget {
        override fun escape(input: String): String = StringEscapeUtils.escapeJson(input)

        override fun unescape(input: String): String = StringEscapeUtils.unescapeJson(input)
    }
}
