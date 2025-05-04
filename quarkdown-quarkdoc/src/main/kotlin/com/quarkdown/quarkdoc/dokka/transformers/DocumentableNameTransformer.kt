package com.quarkdown.quarkdoc.dokka.transformers

import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that renames functions and parameters annotated with `@Name` within the function signature.
 * @see DocumentationNameTransformer for documentation-side renaming
 * @see RenamingsStorage
 */
class DocumentableNameTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        val renaming = RenamingsStorage[function.dri] ?: return function.unchanged()
        return function
            .copy(name = renaming.newName)
            .changed()
    }

    override fun transformParameter(parameter: DParameter): AnyWithChanges<DParameter> {
        val renaming = RenamingsStorage[parameter.dri] ?: return parameter.unchanged()
        return parameter
            .copy(name = renaming.newName)
            .changed()
    }
}
