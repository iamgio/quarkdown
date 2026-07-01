package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.processor.util.callPublic

/**
 * Extracts the verbatim source text of every annotation on a value parameter or function,
 * excluding annotations the processor handles specially, such as `@Name` and `@QFunction`.
 */
internal object AnnotationExtractor {
    /** Simple-name matches skipped from propagation because the processor handles them itself. */
    private val SKIP_SHORT_NAMES = setOf("Name", "QFunction")

    /**
     * Returns the concatenated text of [parameter]'s source annotations (separated by spaces),
     * or `null` when there are none to propagate or PSI cannot be reached.
     */
    fun forParameter(parameter: KSValueParameter): String? =
        runCatching {
            val field = parameter.javaClass.getDeclaredField(KA_VALUE_PARAM_FIELD).also { it.isAccessible = true }
            extract(field.get(parameter))
        }.getOrNull()

    /**
     * Returns the concatenated text of [function]'s source annotations (separated by spaces),
     * or `null` when there are none to propagate or PSI cannot be reached.
     */
    fun forFunction(function: KSFunctionDeclaration): String? =
        runCatching {
            val method = function.javaClass.getMethod(KA_FUNCTION_ACCESSOR).also { it.isAccessible = true }
            extract(method.invoke(function))
        }.getOrNull()

    private fun extract(symbol: Any?): String? {
        if (symbol == null) return null
        val psi = symbol.callPublic(GET_PSI) ?: return null
        val entries = psi.callPublic(GET_ANNOTATION_ENTRIES) as? List<*> ?: return null

        val kept =
            entries
                .filterNotNull()
                .filterNot { it.hasSkippedShortName() }
                .mapNotNull { it.callPublic(GET_TEXT) as? String }

        return kept.joinToString(" ").ifBlank { null }
    }

    private fun Any.hasSkippedShortName(): Boolean {
        val shortName = callPublic(GET_SHORT_NAME) ?: return false
        return (shortName.callPublic(AS_STRING) as? String) in SKIP_SHORT_NAMES
    }

    private const val KA_VALUE_PARAM_FIELD = "ktValueParameterSymbol"

    /** Public accessor on `KSFunctionDeclarationImpl` for the underlying `KaFunctionSymbol`. */
    private const val KA_FUNCTION_ACCESSOR = "getKtFunctionSymbol"

    private const val GET_PSI = "getPsi"
    private const val GET_ANNOTATION_ENTRIES = "getAnnotationEntries"
    private const val GET_TEXT = "getText"
    private const val GET_SHORT_NAME = "getShortName"
    private const val AS_STRING = "asString"
}
