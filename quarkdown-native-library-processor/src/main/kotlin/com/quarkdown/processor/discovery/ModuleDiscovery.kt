package com.quarkdown.processor.discovery

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFile
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.processor.model.ModuleDescriptor
import com.quarkdown.processor.util.hasAnnotation

/**
 * Orchestrates the discovery pipeline for Quarkdown native-library modules in a KSP round:
 * locate `@file:QModule` sources, hand them to [ModuleValidator] for structural checks, then to
 * [ModuleDescriber] to produce the [ModuleDescriptor]s the code generator will consume.
 *
 * Scanning lives here (it depends on the [Resolver]), validation and description are pulled into
 * their own units so each layer has a single responsibility: this class knows *where* symbols
 * come from, the validator knows *which* shapes are legal, and the describer knows *how* to
 * project a KSP symbol into a descriptor.
 */
class ModuleDiscovery(
    private val resolver: Resolver,
    private val logger: KSPLogger,
) {
    /**
     * Returns the descriptors of every well-formed [QModule] file in the current round.
     * Empty modules are reported via [KSPLogger.warn] but still returned so the caller can
     * observe them; structurally-invalid `@QFunction`s already failed the round via [ModuleValidator].
     */
    fun discover(): List<ModuleDescriptor> {
        val moduleFiles: Set<KSFile> = findModuleFiles()
        ModuleValidator(resolver, logger).validate(moduleFiles)

        // Round-scoped registry of @Name exports: populated as functions are described and
        // available downstream (e.g. to the default-value extractor's parameter-rename map).
        val mappings = NameMappings()
        return moduleFiles.map { file ->
            ModuleDescriber.describe(file, mappings).also { descriptor ->
                if (descriptor.functions.isEmpty()) {
                    logger.warn(
                        "@QModule file '${file.fileName}' declares no @QFunction; the generated module will be empty.",
                        file,
                    )
                }
            }
        }
    }

    /**
     * Locates every source file annotated with `@file:QModule`.
     *
     * Iterating [Resolver.getNewFiles] and reading each file's own annotations is more reliable
     * than [Resolver.getSymbolsWithAnnotation] for FILE-targeted annotations, which historically
     * has uneven support across KSP backends.
     */
    private fun findModuleFiles(): Set<KSFile> =
        resolver
            .getNewFiles()
            .filter { it.hasAnnotation<QModule>() }
            .toSet()
}
