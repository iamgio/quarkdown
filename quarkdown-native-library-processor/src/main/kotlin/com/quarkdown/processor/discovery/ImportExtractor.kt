package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFile

/**
 * Extracts the source file's import list so the generated wrapper can carry over whatever the
 * source declared. Imports from the processor's own annotation package are stripped, since the
 * processor consumes those annotations (`@QModule`, `@QFunction`) and doesn't re-emit them on
 * the wrapper - leaving them would produce unused imports.
 */
internal object ImportExtractor : PsiExtractor<KSFile, String> {
    /**
     * Returns the source text of [target]'s import list (minus imports from the processor's own
     * annotation package), or `null` when the file has no imports to propagate.
     */
    override fun extract(
        target: KSFile,
        ctx: DiscoveryContext,
    ): String? {
        val ktFile = ctx.kspPsi.of(target) ?: return null
        val importList = ktFile.get(PsiOps.ImportList) ?: return null
        return importList
            .get(PsiOps.Imports)
            ?.filterNot { it.isProcessorAnnotation() }
            ?.mapNotNull { it.text }
            ?.joinToString(System.lineSeparator())
            ?.ifBlank { null }
    }

    /** True if this `KtImportDirective` targets an annotation in the processor's own package. */
    private fun PsiNode.isProcessorAnnotation(): Boolean {
        val importedFqName = get(PsiOps.ImportedFqName) ?: return false
        val text = importedFqName.get(PsiOps.AsString) ?: return false
        return text.startsWith(PROCESSOR_ANNOTATION_PACKAGE)
    }

    /** Any import whose FQN starts with this prefix is dropped from the wrapper. */
    private const val PROCESSOR_ANNOTATION_PACKAGE = "com.quarkdown.processor.annotation."
}
