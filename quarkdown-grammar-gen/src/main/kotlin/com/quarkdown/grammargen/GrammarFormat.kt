package com.quarkdown.grammargen

import com.quarkdown.core.template.TemplateProcessor

/**
 * A format for generating grammar definitions.
 * @param P The type of named pattern used by this grammar format.
 */
interface GrammarFormat<P : GrammarNamedPattern> {
    /**
     * The base template processor used for generating grammar source files.
     */
    val baseTemplateProcessor: TemplateProcessor

    /**
     * Creates the list of patterns for this format.
     * @return the list of patterns
     */
    fun createPatterns(): List<P>

    /**
     * Converts a pattern to its source code representation.
     * @param pattern the pattern to convert
     * @param isLast whether this is the last pattern in the list (for formatting purposes).
     * @return the source code representation of the pattern
     */
    fun patternToSource(
        pattern: P,
        isLast: Boolean,
    ): String
}
