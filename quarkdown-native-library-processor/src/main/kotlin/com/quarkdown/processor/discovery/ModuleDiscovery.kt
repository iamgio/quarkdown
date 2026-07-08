package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSFile
import com.quarkdown.processor.annotation.QModule
import com.quarkdown.processor.model.ModuleDescriptor
import com.quarkdown.processor.util.hasAnnotation

/**
 * Orchestrates the discovery pipeline for Quarkdown native-library modules in a KSP round:
 * scan `@file:QModule` sources, hand them to [ModuleValidator] for structural checks, then to
 * [ModuleDescriber] to produce the [ModuleDescriptor]s the code generator will consume.
 *
 * Each layer has a single responsibility: this class knows *what order* stages run in and
 * *where warnings live* (e.g. empty modules), the validator knows *which shapes are legal*,
 * and the describer knows *how* to project a KSP symbol into a descriptor. All round-scoped
 * state (resolver, logger, name mappings, discovered files) travels through [DiscoveryContext].
 */
internal class ModuleDiscovery(
    private val ctx: DiscoveryContext,
    private val validator: ModuleValidator = ModuleValidator(),
) {
    /**
     * Returns the descriptors of every well-formed [QModule] file in the current round.
     * Empty modules are reported via `ctx.logger.warn` but still returned so the caller can
     * observe them; structurally-invalid `@QFunction`s already failed the round via [ModuleValidator].
     */
    fun discover(): List<ModuleDescriptor> {
        scanModuleFiles()
        validator.validate(ctx)
        return ctx.moduleFiles.map { file ->
            ModuleDescriber.describe(file, ctx).also { descriptor ->
                if (descriptor.functions.isEmpty()) {
                    ctx.logger.warn(
                        "@QModule file '${file.fileName}' declares no @QFunction; the generated module will be empty.",
                        file,
                    )
                }
            }
        }
    }

    /**
     * Locates every source file annotated with `@file:QModule` and records them into the context.
     *
     * Iterating [ctx.resolver.getNewFiles] and reading each file's own annotations is more reliable
     * than `Resolver.getSymbolsWithAnnotation` for FILE-targeted annotations, which historically
     * has uneven support across KSP backends. On the initial round, `getNewFiles` returns every
     * source file (they are all "new"); on subsequent rounds it returns only the files this or
     * another processor generated, which do not carry `@file:QModule` and so are filtered out.
     * Using [ctx.resolver.getAllFiles] would re-yield the source files on later rounds and cause
     * duplicate wrapper emission.
     */
    private fun scanModuleFiles() {
        ctx.resolver
            .getNewFiles()
            .filter { it.hasAnnotation<QModule>() }
            .forEach { file: KSFile -> ctx.moduleFiles.add(file) }
    }
}
