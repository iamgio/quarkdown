package com.quarkdown.lsp.completion.function

import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter

// Constants for the LSP snippet format.
private const val INSERTION_START = "\${"
private const val INSERTION_DELIMITER = ":"
private const val INSERTION_END = "}"

/**
 * Suffix after each inline argument.
 */
private const val INLINE_ARGUMENT_SUFFIX = " "

/**
 * If the inserted function is a chained call, the parameter with this index is ignored in the snippet.
 */
private const val SKIPPED_PARAMETER_INDEX_IN_CHAINED_CALL = 0

/**
 * Provider of function call snippets for completion, supported by the LSP.
 * @param function the function to generate the snippet from
 */
object FunctionCallInsertionSnippet {
    /**
     * Filters the parameters of a function to include only:
     * - Those that are non-optional (except for the body argument)
     * - The first parameter in a chained call
     */
    private fun filterParameters(
        parameters: List<DocsParameter>,
        chained: Boolean,
    ): List<DocsParameter> =
        parameters.filterIndexed { index, param ->
            if (index == SKIPPED_PARAMETER_INDEX_IN_CHAINED_CALL && chained) {
                // Skips the first parameter if it's a chaining separator.
                return@filterIndexed false
            }
            !param.isOptional || param.isLikelyBody
        }

    /**
     * Generates a snippet for the function call in the accepted format by the LSP.
     * The snippet includes the function name and its non-optional parameters.
     * @param function the function to generate the snippet from
     * @return the function call snippet
     */
    fun forFunction(
        function: DocsFunction,
        chained: Boolean,
    ): String {
        val params = filterParameters(function.parameters, chained)
        var insertionIndex = 1

        return buildString {
            append(function.name)
            append(" ")
            params.forEachIndexed { index, param ->
                // If the parameter is a body parameter, but at least one inline parameter is optional,
                // an additional insertion point is added to improve the user experience,
                // allowing the user to continue typing inline parameters and then switch to the body parameter.
                if (param.isLikelyBody && index != function.parameters.lastIndex) {
                    append(INSERTION_START)
                    append(insertionIndex)
                    append(INSERTION_DELIMITER)
                    append(INSERTION_END)
                    insertionIndex++
                }
                append(
                    forParameter(
                        param,
                        alwaysNamed = false,
                        insertionIndex = insertionIndex,
                        insertionPlaceholder = param.name,
                    ),
                )
                insertionIndex++
            }
        }
    }

    /**
     * Generates a snippet for a function parameter in the accepted format by the LSP.
     * The snippet handles three cases:
     * - Inline, likely unnamed
     * - Inline, likely named
     * - Body
     *
     * @param parameter the parameter to generate the snippet from
     * @param alwaysNamed whether the parameter should always be treated as named if inline
     * @param insertionIndex the index of the insertion point in the snippet
     * @param insertionPlaceholder the placeholder text to insert at the insertion point
     * @return the function parameter snippet
     */
    fun forParameter(
        parameter: DocsParameter,
        alwaysNamed: Boolean,
        insertionIndex: Int = 1,
        insertionPlaceholder: String = "",
    ): String =
        with(QuarkdownPatterns.FunctionCall) {
            // If the parameter has fixed values, generates a snippet with a|b|c as the placeholder.
            val valuesPlaceholder: String? = parameter.allowedValues?.joinToString(separator = "|")
            // The actual insertion.
            // If there are fixed values, the result will be `a|b|c` if the placeholder is empty,
            // or `placeholder (a|b|c)` otherwise.
            val insertion =
                buildString {
                    append(INSERTION_START)
                    append(insertionIndex)
                    append(INSERTION_DELIMITER)
                    append(insertionPlaceholder)
                    valuesPlaceholder?.let {
                        if (insertionPlaceholder.isEmpty()) {
                            append(it)
                        } else {
                            append(" (")
                            append(it)
                            append(")")
                        }
                    }
                    append(INSERTION_END)
                }

            // {abc}
            val inlineArgument = ARGUMENT_BEGIN + insertion + ARGUMENT_END + INLINE_ARGUMENT_SUFFIX

            when {
                parameter.isLikelyBody ->
                    "\n" + CONVENTIONAL_BODY_INDENT + insertion

                // name:{abc}
                alwaysNamed || parameter.isLikelyNamed ->
                    parameter.name + NAMED_ARGUMENT_DELIMITER + inlineArgument

                else ->
                    inlineArgument
            }
        }
}
