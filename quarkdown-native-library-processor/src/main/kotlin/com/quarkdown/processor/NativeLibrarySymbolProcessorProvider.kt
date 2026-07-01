package com.quarkdown.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * KSP entry point that instantiates [NativeLibrarySymbolProcessor].
 */
class NativeLibrarySymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor =
        NativeLibrarySymbolProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
        )
}
