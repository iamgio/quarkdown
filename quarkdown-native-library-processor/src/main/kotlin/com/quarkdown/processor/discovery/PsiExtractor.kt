package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSAnnotated

/**
 * Strategy for pulling a piece of source-level information off a KSP declaration through PSI.
 *
 * Every extractor in the discovery pipeline (default expressions, source annotations, import
 * lists) conforms to this shape, so:
 * - the [ModuleDescriber] uses a uniform call form regardless of which piece it's fetching;
 * - future needs (KDoc harvesting for Quarkdoc, source-location capture for diagnostics,
 *   document-type constraint metadata, ...) plug in as another [PsiExtractor] rather than as
 *   a bespoke object with its own method signature.
 *
 * All state a stage might need at extraction time - the reflective PSI facade, the round's name
 * mappings, the logger - reaches the extractor through the [DiscoveryContext] parameter, so the
 * interface itself stays a two-argument function.
 *
 * @param T KSP declaration type this extractor accepts (`KSFile`, `KSValueParameter`, ...).
 * @param R the extracted representation (typically `String`, the verbatim source-level text).
 */
internal fun interface PsiExtractor<in T : KSAnnotated, out R : Any> {
    /**
     * Returns the extracted value for [target], or `null` when the source declares nothing to
     * extract (e.g. a parameter with no default) or PSI is unreachable (reflection failed,
     * KSP2's shaded API changed).
     */
    fun extract(
        target: T,
        ctx: DiscoveryContext,
    ): R?
}
