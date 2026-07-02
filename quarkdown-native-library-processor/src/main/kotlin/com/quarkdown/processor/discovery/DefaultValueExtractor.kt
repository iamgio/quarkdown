package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.processor.discovery.DefaultValueExtractor.extract

/**
 * Extracts the source-level default expression of a [KSValueParameter] by reading the underlying
 * PSI `KtParameter` through the [KspPsi] / [PsiNode] abstraction.
 *
 * KSP's public symbol API exposes only [KSValueParameter.hasDefault]; the expression itself has
 * to come from the PSI KSP holds internally. If KSP renames the backing field or moves to a
 * non-PSI backend, [extract] returns `null` uniformly and the wrapper loses `isOptional`, which
 * is loud (`InvalidArgumentCountException` on any call site that omits the default).
 */
internal object DefaultValueExtractor {
    /**
     * Returns the source-level default expression of [parameter], with references to any
     * parameter listed in [parameterRenames] (original name -> exported name) rewritten to the
     * exported name. Returns `null` when the parameter has no default or PSI cannot be reached.
     */
    fun extract(
        parameter: KSValueParameter,
        parameterRenames: Map<String, String> = emptyMap(),
    ): String? {
        if (!parameter.hasDefault) return null
        val ktParameter = KspPsi.of(parameter) ?: return null
        val defaultValue = ktParameter.asNode(GET_DEFAULT_VALUE) ?: return null
        return applyRenames(defaultValue, parameterRenames)
    }

    /**
     * Substitutes any `KtNameReferenceExpression` under [expression] whose name is in [renames]
     * with its mapped replacement. Substitutions are applied in reverse offset order so earlier
     * positions stay valid as the string is rewritten.
     */
    private fun applyRenames(
        expression: PsiNode,
        renames: Map<String, String>,
    ): String? {
        val text = expression.text ?: return null
        if (renames.isEmpty()) return text

        val baseOffset = expression.startOffset ?: return text
        val substitutions =
            expression
                .walk()
                .filter { it.simpleName == KT_NAME_REFERENCE_EXPRESSION }
                .mapNotNull { node -> renameOf(node, baseOffset, renames) }
                .sortedByDescending { it.offset }
                .toList()
        if (substitutions.isEmpty()) return text

        val builder = StringBuilder(text)
        substitutions.forEach { sub ->
            builder.replace(sub.offset, sub.offset + sub.length, sub.replacement)
        }
        return builder.toString()
    }

    private fun renameOf(
        node: PsiNode,
        baseOffset: Int,
        renames: Map<String, String>,
    ): Substitution? {
        val name = node.call(GET_REFERENCED_NAME) as? String ?: return null
        val replacement = renames[name] ?: return null
        val start = node.startOffset ?: return null
        return Substitution(offset = start - baseOffset, length = name.length, replacement = replacement)
    }

    private data class Substitution(
        val offset: Int,
        val length: Int,
        val replacement: String,
    )

    private const val GET_DEFAULT_VALUE = "getDefaultValue"
    private const val GET_REFERENCED_NAME = "getReferencedName"
    private const val KT_NAME_REFERENCE_EXPRESSION = "KtNameReferenceExpression"
}
