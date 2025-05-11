package com.quarkdown.quarkdoc.dokka.signature

import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DFunction

private const val MIN_PARAMETERS_TO_SPLIT_LINES = 3

/**
 * Strategy that defines line breaks in the function signature.
 * @see QuarkdownSignatureProvider
 */
interface LineBreakingStrategy {
    /**
     * Defines line breaking before each function parameter.
     */
    fun PageContentBuilder.DocumentableContentBuilder.beforeParameter(index: Int)

    /**
     * Defines line breaking before the return type.
     */
    fun PageContentBuilder.DocumentableContentBuilder.beforeReturn()

    companion object {
        /**
         * Creates a [LineBreakingStrategy] based on the function's parameter count.
         * @param function the function to analyze
         * @return a coherent [LineBreakingStrategy] instance
         */
        fun fromFunction(function: DFunction): LineBreakingStrategy {
            val splitLines = function.parameters.size >= MIN_PARAMETERS_TO_SPLIT_LINES
            return when {
                splitLines -> SplitLineBreakingStrategy(function)
                else -> NoLineBreakingStrategy()
            }
        }
    }
}

/**
 * No line breaking.
 */
private class NoLineBreakingStrategy : LineBreakingStrategy {
    override fun PageContentBuilder.DocumentableContentBuilder.beforeParameter(index: Int) {}

    override fun PageContentBuilder.DocumentableContentBuilder.beforeReturn() {}
}

/**
 * Adds a line break and indents each parameter.
 */
private class SplitLineBreakingStrategy(
    private val function: DFunction,
) : LineBreakingStrategy {
    override fun PageContentBuilder.DocumentableContentBuilder.beforeParameter(index: Int) {
        if (index == 0) return
        breakLine()
        punctuation(" ".repeat(BEGIN.length + function.name.length))
    }

    override fun PageContentBuilder.DocumentableContentBuilder.beforeReturn() {
        breakLine()
    }
}
