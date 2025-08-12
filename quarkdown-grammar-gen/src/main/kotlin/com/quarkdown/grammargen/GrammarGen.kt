package com.quarkdown.grammargen

private const val PATTERNS_PLACEHOLDER = "PATTERN"

/**
 * Generator of Quarkdown grammar sources for various formats, such as TextMate.
 */
object GrammarGen {
    /**
     * Generates the grammar source code based on the provided format.
     * @param format the grammar format to use for generation
     * @return the generated grammar source code
     */
    fun <P : GrammarNamedPattern> generate(format: GrammarFormat<P>): String {
        val patterns = format.createPatterns()
        val templateProcessor = format.baseTemplateProcessor
        val patternSources =
            patterns.mapIndexed { index, pattern ->
                val isLast = index == patterns.lastIndex
                format.patternToSource(pattern, isLast)
            }

        return templateProcessor
            .iterable(PATTERNS_PLACEHOLDER, patternSources)
            .process()
            .toString()
    }
}
