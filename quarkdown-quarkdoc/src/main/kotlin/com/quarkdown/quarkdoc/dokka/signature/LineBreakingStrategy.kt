package com.quarkdown.quarkdoc.dokka.signature

import org.jetbrains.dokka.base.translators.documentables.PageContentBuilder
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import kotlin.math.max

private const val MIN_PARAMETERS_TO_SPLIT_LINES = 3

/**
 * Strategy that defines line breaks in the function signature.
 * @see QuarkdownSignatureProvider
 */
interface LineBreakingStrategy {
    /**
     * Defines line breaking before each function parameter.
     * @param parameter the parameter to create content for
     * @param index the index of the parameter
     */
    fun PageContentBuilder.DocumentableContentBuilder.beforeParameter(
        parameter: DParameter,
        index: Int,
    )

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
    override fun PageContentBuilder.DocumentableContentBuilder.beforeParameter(
        parameter: DParameter,
        index: Int,
    ) {}

    override fun PageContentBuilder.DocumentableContentBuilder.beforeReturn() {
        punctuation(" ")
    }
}

/**
 * Adds a line break and indents each parameter.
 */
private class SplitLineBreakingStrategy(
    private val function: DFunction,
) : LineBreakingStrategy {
    private val firstParameterNameLength =
        function.parameters
            .firstOrNull()
            ?.name
            ?.length ?: 0

    private val maxParameterNameLength =
        function.parameters.maxOfOrNull { it.name?.length ?: 0 } ?: 0

    private val minPad = BEGIN.length + function.name.length

    private fun PageContentBuilder.DocumentableContentBuilder.pad(size: Int) {
        if (size <= 0) return
        punctuation(" ".repeat(size))
    }

    override fun PageContentBuilder.DocumentableContentBuilder.beforeParameter(
        parameter: DParameter,
        index: Int,
    ) {
        val parameterNameLength = parameter.name?.length ?: 0
        val supplementPad = max(minPad, maxParameterNameLength - firstParameterNameLength)
        if (index != 0) {
            breakLine()
            pad(
                supplementPad + (firstParameterNameLength - parameterNameLength),
            )
        } else {
            pad(supplementPad - minPad)
        }
    }

    override fun PageContentBuilder.DocumentableContentBuilder.beforeReturn() {
        breakLine()
    }
}
