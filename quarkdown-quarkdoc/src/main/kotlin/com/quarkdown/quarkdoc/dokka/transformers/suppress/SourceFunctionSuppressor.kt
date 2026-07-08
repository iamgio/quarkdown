package com.quarkdown.quarkdoc.dokka.transformers.suppress

import com.quarkdown.processor.annotation.QFunction
import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocDocumentableReplacerTransformer
import com.quarkdown.quarkdoc.dokka.util.hasAnnotation
import org.jetbrains.dokka.model.DPackage
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Drops the source-side `@QFunction`s from the documentation model so the KSP-generated wrapper
 * (`object <ModuleName> { public fun ... }`) becomes the sole documented entry point.
 */
class SourceFunctionSuppressor(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformPackage(pkg: DPackage): AnyWithChanges<DPackage> {
        val retained = pkg.functions.filterNot { it.hasAnnotation<QFunction>() }
        if (retained.size == pkg.functions.size) {
            return pkg.unchanged()
        }
        return pkg.copy(functions = retained).changed()
    }
}
