package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFile
import com.quarkdown.processor.util.callPublic

/**
 * Extracts the source file's import list, so the generated wrapper can carry over whatever the
 * source declared.
 */
internal object ImportExtractor {
    /** Any import whose FQN starts with this prefix is dropped from the wrapper. */
    private const val PROCESSOR_ANNOTATION_PACKAGE = "com.quarkdown.processor.annotation."

    /**
     * Returns the verbatim source text of [file]'s import list (minus imports from the processor's
     * own annotation package), or `null` when the file has no imports to propagate.
     */
    fun extract(file: KSFile): String? =
        runCatching {
            val field = file.javaClass.getDeclaredField(KA_SYMBOL_FIELD).also { it.isAccessible = true }
            val symbol = field.get(file) ?: return@runCatching null
            val psi = symbol.callPublic(GET_PSI) ?: return@runCatching null
            val importList = psi.callPublic(GET_IMPORT_LIST) ?: return@runCatching null
            val entries = importList.callPublic(GET_IMPORTS) as? List<*> ?: return@runCatching null

            entries
                .filterNotNull()
                .filterNot { it.isProcessorAnnotation() }
                .mapNotNull { it.callPublic(GET_TEXT) as? String }
                .joinToString(System.lineSeparator())
                .ifBlank { null }
        }.getOrNull()

    /**
     * True if this `KtImportDirective` targets an annotation in the processor's own package
     * (i.e. `@QModule` or `@QFunction`).
     */
    private fun Any.isProcessorAnnotation(): Boolean {
        val importedFqName = callPublic(GET_IMPORTED_FQ_NAME) ?: return false
        val fqn = importedFqName.callPublic(AS_STRING) as? String ?: return false
        return fqn.startsWith(PROCESSOR_ANNOTATION_PACKAGE)
    }

    /** Private field on `com.google.devtools.ksp.impl.symbol.kotlin.KSFileImpl`. */
    private const val KA_SYMBOL_FIELD = "ktFileSymbol"
    private const val GET_PSI = "getPsi"
    private const val GET_IMPORT_LIST = "getImportList"
    private const val GET_IMPORTS = "getImports"
    private const val GET_IMPORTED_FQ_NAME = "getImportedFqName"
    private const val GET_TEXT = "getText"
    private const val AS_STRING = "asString"
}
