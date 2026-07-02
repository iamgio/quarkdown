package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

/**
 * Extracts the verbatim source text of every annotation on a value parameter or function,
 * skipping annotations the processor handles specially: `@Name` (applied at the symbol level by
 * renaming) and `@QFunction` (the processor's own marker, meaningless on the generated wrapper).
 */
internal object AnnotationExtractor {
    /** Simple-name matches skipped from propagation because the processor handles them itself. */
    private val SKIP_SHORT_NAMES = setOf("Name", "QFunction")

    /** Concatenated text of [parameter]'s source annotations, or `null` if none apply. */
    fun forParameter(parameter: KSValueParameter): String? = extract(KspPsi.of(parameter))

    /** Concatenated text of [function]'s source annotations, or `null` if none apply. */
    fun forFunction(function: KSFunctionDeclaration): String? = extract(KspPsi.of(function))

    private fun extract(psi: PsiNode?): String? {
        if (psi == null) return null
        return psi
            .asList(GET_ANNOTATION_ENTRIES)
            .filterNot { it.isProcessorManaged() }
            .mapNotNull { it.text }
            .joinToString(" ")
            .ifBlank { null }
    }

    /** True if this `KtAnnotationEntry`'s short name is one the processor consumes internally. */
    private fun PsiNode.isProcessorManaged(): Boolean {
        val shortName = asNode(GET_SHORT_NAME) ?: return false
        return (shortName.call(AS_STRING) as? String) in SKIP_SHORT_NAMES
    }

    private const val GET_ANNOTATION_ENTRIES = "getAnnotationEntries"
    private const val GET_SHORT_NAME = "getShortName"
    private const val AS_STRING = "asString"
}
