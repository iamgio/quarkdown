package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFile

/**
 * Extracts the source file's import list so the generated wrapper can carry over whatever the
 * source declared. Imports from the processor's own annotation package are stripped, since the
 * processor consumes those annotations (`@QModule`, `@QFunction`) and doesn't re-emit them on
 * the wrapper - leaving them would produce unused imports.
 */
internal object ImportExtractor {
    /** Any import whose FQN starts with this prefix is dropped from the wrapper. */
    private const val PROCESSOR_ANNOTATION_PACKAGE = "com.quarkdown.processor.annotation."

    /**
     * Returns the source text of [file]'s import list (minus imports from the processor's own
     * annotation package), or `null` when the file has no imports to propagate.
     */
    fun extract(file: KSFile): String? {
        val ktFile = KspPsi.of(file) ?: return null
        val importList = ktFile.asNode(GET_IMPORT_LIST) ?: return null
        return importList
            .asList(GET_IMPORTS)
            .filterNot { it.isProcessorAnnotation() }
            .mapNotNull { it.text }
            .joinToString(System.lineSeparator())
            .ifBlank { null }
    }

    /** True if this `KtImportDirective` targets an annotation in the processor's own package. */
    private fun PsiNode.isProcessorAnnotation(): Boolean {
        val importedFqName = asNode(GET_IMPORTED_FQ_NAME) ?: return false
        val text = importedFqName.call(AS_STRING) as? String ?: return false
        return text.startsWith(PROCESSOR_ANNOTATION_PACKAGE)
    }

    private const val GET_IMPORT_LIST = "getImportList"
    private const val GET_IMPORTS = "getImports"
    private const val GET_IMPORTED_FQ_NAME = "getImportedFqName"
    private const val AS_STRING = "asString"
}
