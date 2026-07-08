package com.quarkdown.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.quarkdown.processor.discovery.LoggingPsiDiagnostics
import com.quarkdown.processor.discovery.PsiDiagnostics

/**
 * KSP entry point that instantiates [NativeLibrarySymbolProcessor].
 *
 * Also honors the `quarkdown.psi.debug=true` processor option, which turns on
 * [LoggingPsiDiagnostics] so every reflective PSI failure is surfaced through the KSP logger.
 * Intended as a diagnostic aid during KSP upgrades; disabled by default because a swallowed
 * failure typically means "extractor could not read some optional piece of source" and is not
 * user-actionable.
 */
class NativeLibrarySymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val diagnostics: PsiDiagnostics =
            if (environment.options[PSI_DEBUG_OPTION] == "true") {
                LoggingPsiDiagnostics(environment.logger)
            } else {
                PsiDiagnostics.NoOp
            }
        return NativeLibrarySymbolProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
            psiDiagnostics = diagnostics,
        )
    }

    private companion object {
        const val PSI_DEBUG_OPTION = "quarkdown.psi.debug"
    }
}
