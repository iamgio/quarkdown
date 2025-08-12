package com.quarkdown.lsp.completion

import com.quarkdown.lsp.completion.FunctionCallInsertionSnippet.forFunction
import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.quarkdoc.reader.DocsFunction
import com.quarkdown.quarkdoc.reader.DocsParameter
import com.quarkdown.quarkdoc.reader.DocsWalker

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
                val insertion = "\${${index + 1}:${param.name}}"

                if (!param.isLikelyBody) {
                    append(" ")
                }
                append(forParameter(param, alwaysNamed = false, insertion = insertion))
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
     * @param insertion the insertion text to use in the snippet. Defaults to an empty placeholder.
     * @return the function parameter snippet
     */
    fun forParameter(
        parameter: DocsParameter,
        alwaysNamed: Boolean,
        insertion: String = "\${1:}",
    ): String =
        with(QuarkdownPatterns.FunctionCall) {
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
