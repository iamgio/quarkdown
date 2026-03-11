package com.quarkdown.core.lexer.regex

import com.quarkdown.core.util.replace

/**
 * A builder for [Regex] patterns.
 * @param baseRegex initial pattern
 */
class RegexBuilder(
    baseRegex: String,
) {
    private val pattern = StringBuilder(baseRegex)

    /**
     * Adds a reference to the pattern. All the occurrences will be replaced.
     * @param label text to be replaced
     * @param regex new pattern to replace the label with
     * @return this for concatenation
     */
    fun withReference(
        label: String,
        regex: String,
    ) = apply {
        pattern.replace(label, regex)
    }

    /**
     * @return the raw pattern string
     */
    fun build(): String = pattern.toString()

    /**
     * @return a new [Regex] with the given [options].
     */
    fun buildRegex(vararg options: RegexOption): Regex = build().toRegex(options.toSet())
}
