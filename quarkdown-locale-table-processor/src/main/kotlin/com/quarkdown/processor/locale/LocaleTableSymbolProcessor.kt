package com.quarkdown.processor.locale

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * KSP processor that generates the locale name tables at build time
 * from the build JDK's CLDR locale data, so that locale resolution and display names
 * are available at runtime on any platform without depending on `java.util.Locale`.
 * See [LocaleTableCodeGenerator] for the emitted artifact.
 *
 * The processor reads no source symbols: it emits its single output on the first round,
 * unconditionally.
 */
class LocaleTableSymbolProcessor(
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (!invoked) {
            invoked = true
            codeGenerator
                .createNewFile(Dependencies(aggregating = false), PACKAGE_NAME, FILE_NAME)
                .bufferedWriter()
                .use { it.write(LocaleTableCodeGenerator().buildSource()) }
        }
        return emptyList()
    }
}
