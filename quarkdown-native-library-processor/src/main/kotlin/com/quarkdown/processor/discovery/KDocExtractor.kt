package com.quarkdown.processor.discovery

import com.google.devtools.ksp.symbol.KSDeclaration

/**
 * Reads the KDoc body of a [KSDeclaration] as KSP hands it back: inner text with the `/** */`
 * markers and per-line `*` prefix already stripped, ready for
 * [com.quarkdown.processor.generation.KDocRewriter].
 * Kept on the [PsiExtractor] shape (though [KSDeclaration.docString] does not need PSI access) so
 * the describer calls every extractor uniformly.
 */
internal object KDocExtractor : PsiExtractor<KSDeclaration, String> {
    override fun extract(
        target: KSDeclaration,
        ctx: DiscoveryContext,
    ): String? = target.docString?.trim()?.takeIf { it.isNotEmpty() }
}
