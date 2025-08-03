package com.quarkdown.lsp.completion

import com.quarkdown.lsp.pattern.QuarkdownPatterns
import com.quarkdown.quarkdoc.reader.DocsParameter
import com.quarkdown.quarkdoc.reader.DocsWalker

/**
 * Provider of function call snippets for completion.
 *
 * When a function is selected for completion, a snippet is generated that includes
 * the function name and its non-optional parameters.
 *
 * @param function the function to generate the snippet from
 */
class FunctionCallSnippet(
    private val function: DocsWalker.Result<*>,
) {
    private fun getParameters(): List<DocsParameter> =
        this.function
            .extractor()
            .extractFunctionData()
            ?.parameters
            ?.filter { !it.isOptional && !it.isLikelyBody }
            ?: emptyList()

    /**
     * Generates a snippet for the function call in the accepted format by the LSP.
     * @return the function call snippet
     */
    fun getAsString(): String {
        val params = getParameters()

        return buildString {
            append(function.name)
            params.forEachIndexed { index, param ->
                val snippetArg = "\${${index + 1}:${param.name}}"
                when {
                    param.isLikelyBody -> append("\n    $snippetArg")
                    else -> {
                        append(" ")
                        if (param.isLikelyNamed) {
                            append(param.name)
                            append(QuarkdownPatterns.FunctionCall.NAMED_ARGUMENT_DELIMITER)
                        }
                        append(QuarkdownPatterns.FunctionCall.ARGUMENT_BEGIN)
                        append(snippetArg)
                        append(QuarkdownPatterns.FunctionCall.ARGUMENT_END)
                    }
                }
            }
        }
    }
}
