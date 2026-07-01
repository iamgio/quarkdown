package com.quarkdown.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.quarkdown.processor.discovery.ModuleDiscovery
import com.quarkdown.processor.generation.ModuleCodeGenerator

/**
 * KSP processor that discovers Quarkdown native library modules and emits their boilerplate at build time.
 *
 * - Every `@file:QModule` source becomes a `QuarkdownModule`
 * - Every in-file `@QFunction` becomes one of its exported functions.
 *
 * Discovery is delegated to [ModuleDiscovery] and code emission to [ModuleCodeGenerator].
 */
class NativeLibrarySymbolProcessor(
    private val logger: KSPLogger,
    codeGenerator: CodeGenerator,
) : SymbolProcessor {
    private val moduleGenerator = ModuleCodeGenerator(codeGenerator)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val modules = ModuleDiscovery(resolver, logger).discover()

        modules.forEach { module ->
            moduleGenerator.generate(module)
            logger.info(
                "Generated Quarkdown module '${module.name}' (${module.packageName}) " +
                    "with ${module.functions.size} function(s): " +
                    module.functions.joinToString(", ") { it.exportedName },
            )
        }

        return emptyList()
    }
}
