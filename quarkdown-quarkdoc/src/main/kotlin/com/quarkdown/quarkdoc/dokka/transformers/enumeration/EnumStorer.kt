package com.quarkdown.quarkdoc.dokka.transformers.enumeration

import com.quarkdown.quarkdoc.dokka.transformers.QuarkdocDocumentableReplacerTransformer
import com.quarkdown.quarkdoc.dokka.transformers.module.QuarkdownModulesStorage
import org.jetbrains.dokka.model.DClasslike
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that, instead of performing transformations, stores enum declarations within the module this plugin is applied on.
 * @see QuarkdownModulesStorage
 * @see EnumParameterEntryListerTransformer
 */
class EnumStorer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformClassLike(classlike: DClasslike): AnyWithChanges<DClasslike> {
        if (classlike is DEnum) {
            EnumStorage += classlike
        }
        return classlike.unchanged()
    }
}
