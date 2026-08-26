package com.quarkdown.processor.locale

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * KSP entry point that instantiates [LocaleTableSymbolProcessor].
 */
class LocaleTableSymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor = LocaleTableSymbolProcessor(environment.codeGenerator)
}
