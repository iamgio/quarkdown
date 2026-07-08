package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.quarkdown.processor.discovery.AnnotationExtractor.ForFunction
import com.quarkdown.processor.discovery.AnnotationExtractor.ForParameter

/**
 * Extractors for the verbatim source text of function-level and parameter-level annotations,
 * skipping the ones the processor handles specially: `@Name` (applied at the symbol level by
 * renaming) and `@QFunction` (the processor's own marker, meaningless on the generated wrapper).
 *
 * Split into two typed [PsiExtractor]s - [ForFunction] and [ForParameter] - so callers get
 * compile-time targeting rather than a single method that would take either of two types.
 */
internal object AnnotationExtractor {
    /** Concatenated text of a function's source annotations, or `null` if none apply. */
    val ForFunction: PsiExtractor<KSFunctionDeclaration, String> =
        PsiExtractor { target, ctx -> extract(ctx.kspPsi.of(target)) }

    /** Concatenated text of a value parameter's source annotations, or `null` if none apply. */
    val ForParameter: PsiExtractor<KSValueParameter, String> =
        PsiExtractor { target, ctx -> extract(ctx.kspPsi.of(target)) }

    private fun extract(psi: PsiNode?): String? {
        if (psi == null) return null
        return psi
            .get(PsiOps.AnnotationEntries)
            ?.filterNot { it.isProcessorManaged() }
            ?.mapNotNull { it.text }
            ?.joinToString(" ")
            ?.ifBlank { null }
    }

    /** True if this `KtAnnotationEntry`'s short name is one the processor consumes internally. */
    private fun PsiNode.isProcessorManaged(): Boolean {
        val shortName = get(PsiOps.ShortName) ?: return false
        return shortName.get(PsiOps.AsString) in SKIP_SHORT_NAMES
    }

    /** Simple-name matches skipped from propagation because the processor handles them itself. */
    private val SKIP_SHORT_NAMES = setOf("Name", "QFunction", "Spread")
}
