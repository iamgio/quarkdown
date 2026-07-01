package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.processor.discovery.DefaultValueExtractor.extract
import com.quarkdown.processor.util.callPublic

/**
 * Extracts the source-level default expression of a [KSValueParameter] by reaching into KSP2's
 * underlying Kotlin Analysis API symbol and reading the PSI `KtParameter`.
 *
 * Reflection is used for two reasons: `KSValueParameterImpl.ktValueParameterSymbol` is private,
 * and `KaValueParameterSymbol` lives in KSP's shaded `ksp.org.jetbrains.kotlin.*` package which
 * cannot be referenced from source without coupling to KSP's internal layout. If KSP renames the
 * field or moves to a non-PSI backend, [extract] returns `null` uniformly; the wrapper then loses
 * `isOptional`, which is loud (`InvalidArgumentCountException` on any call site that omits the
 * default) rather than silent.
 */
internal object DefaultValueExtractor {
    /**
     * Returns the source-level default expression of [parameter], with references to any
     * parameter listed in [parameterRenames] (original name -> exported name) rewritten to the
     * exported name. Returns `null` when the parameter has no default, the underlying KSP impl
     * is unrecognized, or the parameter has no associated source PSI.
     */
    fun extract(
        parameter: KSValueParameter,
        parameterRenames: Map<String, String> = emptyMap(),
    ): String? {
        if (!parameter.hasDefault) return null
        val field =
            runCatching {
                parameter.javaClass.getDeclaredField(KA_SYMBOL_FIELD).also { it.isAccessible = true }
            }.getOrNull() ?: return null

        return runCatching {
            val symbol = field.get(parameter) ?: return@runCatching null
            val psi = symbol.callPublic(GET_PSI) ?: return@runCatching null
            val defaultValue = psi.callPublic(GET_DEFAULT_VALUE) ?: return@runCatching null
            applyRenames(defaultValue, parameterRenames)
        }.getOrNull()
    }

    /**
     * Substitutes any `KtNameReferenceExpression` under [expression] whose name is in [renames]
     * with its mapped replacement. Substitutions are applied in reverse offset order so earlier
     * positions stay valid as the string is rewritten. Returns `null` if the expression's text
     * cannot be read, so [extract] degrades to "no default" rather than crashing.
     */
    private fun applyRenames(
        expression: Any,
        renames: Map<String, String>,
    ): String? {
        val text = expression.callPublic(GET_TEXT) as? String ?: return null
        if (renames.isEmpty()) return text

        val baseOffset = expression.psiStartOffset() ?: return text
        val substitutions = mutableListOf<Substitution>()
        collectNameReferences(expression, baseOffset, renames, substitutions)
        if (substitutions.isEmpty()) return text

        val builder = StringBuilder(text)
        substitutions.sortedByDescending { it.offset }.forEach { sub ->
            builder.replace(sub.offset, sub.offset + sub.length, sub.replacement)
        }
        return builder.toString()
    }

    /**
     * Walks the PSI tree below [element] and records every `KtNameReferenceExpression` whose
     * referenced name appears in [renames]. Class matching is by simple name to insulate against
     * KSP's shaded package prefix.
     */
    private fun collectNameReferences(
        element: Any,
        baseOffset: Int,
        renames: Map<String, String>,
        out: MutableList<Substitution>,
    ) {
        if (element.javaClass.simpleName == KT_NAME_REFERENCE_EXPRESSION) {
            val name = element.callPublic(GET_REFERENCED_NAME) as? String
            val replacement = name?.let(renames::get)
            val start = element.psiStartOffset()
            if (replacement != null && start != null) {
                out += Substitution(start - baseOffset, name.length, replacement)
            }
        }
        val children = element.callPublic(GET_CHILDREN) as? Array<*> ?: return
        for (child in children) {
            if (child != null) collectNameReferences(child, baseOffset, renames, out)
        }
    }

    private fun Any.psiStartOffset(): Int? {
        val range = callPublic(GET_TEXT_RANGE) ?: return null
        return range.callPublic(GET_START_OFFSET) as? Int
    }

    private data class Substitution(
        val offset: Int,
        val length: Int,
        val replacement: String,
    )

    /**
     * Private field on `com.google.devtools.ksp.impl.symbol.kotlin.KSValueParameterImpl` that
     * holds the underlying `KaValueParameterSymbol` in KSP2.
     */
    private const val KA_SYMBOL_FIELD = "ktValueParameterSymbol"

    // Reflective method names on the KSP-shaded Analysis API and Kotlin PSI types.
    private const val GET_PSI = "getPsi"
    private const val GET_DEFAULT_VALUE = "getDefaultValue"
    private const val GET_TEXT = "getText"
    private const val GET_TEXT_RANGE = "getTextRange"
    private const val GET_START_OFFSET = "getStartOffset"
    private const val GET_CHILDREN = "getChildren"
    private const val GET_REFERENCED_NAME = "getReferencedName"
    private const val KT_NAME_REFERENCE_EXPRESSION = "KtNameReferenceExpression"
}
