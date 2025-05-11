package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.quarkdoc.dokka.storage.RenamingsStorage
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that renames functions and parameters annotated with `@Name` within the function signature.
 * @see DocumentationNameTransformer for documentation-side renaming
 * @see RenamingsStorage
 */
class DocumentableNameTransformer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    private fun <D : Documentable> rename(
        documentable: D,
        copy: (String) -> D,
    ): AnyWithChanges<D> {
        val renaming = RenamingsStorage[documentable.dri] ?: return documentable.unchanged()
        return copy(renaming.newName).changed()
    }

    override fun transformFunction(function: DFunction) = rename(function) { name -> function.copy(name = name) }

    override fun transformParameter(parameter: DParameter) = rename(parameter) { name -> parameter.copy(name = name) }
}
