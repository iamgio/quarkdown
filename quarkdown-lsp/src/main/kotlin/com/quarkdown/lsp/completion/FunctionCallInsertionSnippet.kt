package com.quarkdown.lsp.completion

import com.quarkdown.lsp.completion.FunctionCallInsertionSnippet.forFunction
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter
import com.quarkdown.quarkdoc.reader.DocsWalker

// Constants for the LSP snippet format.
private const val INSERTION_START = "\${"
private const val INSERTION_DELIMITER = ":"
private const val INSERTION_END = "}"

/**
 * Provider of function call snippets for completion, supported by the LSP.
 * @param function the function to generate the snippet from
 */
object FunctionCallInsertionSnippet {
    /**
     * Generates a snippet for the function call in the accepted format by the LSP.
     * The snippet includes the function name and its non-optional parameters.
     * @param function the function to generate the snippet from
     * @return the function call snippet
     */
    fun forFunction(function: DocsFunction): String {
        val params = function.parameters.filter { !it.isOptional || it.isLikelyBody }

        return buildString {
            append(function.name)
            params.forEachIndexed { index, param ->
                if (!param.isLikelyBody) {
                    append(" ")
                }
                append(
                    forParameter(
                        param,
                        alwaysNamed = false,
                        insertionIndex = index + 1,
                        insertionPlaceholder = param.name,
                    ),
                )
            }
        }
    }

    /**
     * @see forFunction(DocsFunction)
     */
    fun forFunction(function: DocsWalker.Result<*>): String {
        val data = function.extractor().extractFunctionData() ?: return ""
        return forFunction(data)
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

            when {
                parameter.isLikelyBody ->
                    "\n" + CONVENTIONAL_BODY_INDENT + insertion

                alwaysNamed || parameter.isLikelyNamed ->
                    parameter.name + NAMED_ARGUMENT_DELIMITER + ARGUMENT_BEGIN + insertion + ARGUMENT_END

                else ->
                    ARGUMENT_BEGIN + insertion + ARGUMENT_END
            }
        }
}
