package com.quarkdown.processor.discovery

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSFile

/**
 * Round-scoped state carried through the discovery pipeline.
 *
 * Every stage (scanning, validation, description) and every [PsiExtractor] receives the same
 * context, so anything that needs to persist across the round - the KSP [Resolver], the
 * [KSPLogger], the reflective PSI facade, the discovered module files, the accumulated
 * [NameMappings] - lives in one place rather than being threaded through per-method
 * parameter lists.
 *
 * Mirrors the way `Context` is used across `quarkdown-core`: single object, mutated cooperatively
 * by stages that agree on where each piece of state lives.
 *
 * @param resolver KSP resolver for the current round; used for scanning and validation
 * @param logger sink for diagnostics reported by any stage
 * @param kspPsi reflective PSI facade, pre-wired with the round's diagnostic sink
 * @param mappings round-scoped registry of `@Name` exports, populated as functions and parameters
 *   are described and read downstream by extractors that need name-translation
 * @param moduleFiles files identified as `@file:QModule`, populated by the scanning stage and read
 *   by validators (to detect orphaned `@QFunction`s) and describers (to build descriptors)
 */
internal class DiscoveryContext(
    val resolver: Resolver,
    val logger: KSPLogger,
    val kspPsi: KspPsi,
    val mappings: NameMappings = NameMappings(),
    val moduleFiles: MutableSet<KSFile> = mutableSetOf(),
)
