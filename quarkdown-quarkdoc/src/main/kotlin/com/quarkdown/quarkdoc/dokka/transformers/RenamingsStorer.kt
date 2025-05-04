package com.quarkdown.quarkdoc.dokka.transformers

import com.quarkdown.quarkdoc.dokka.storage.Renaming
import com.quarkdown.quarkdoc.dokka.storage.RenamingsStorage
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.model.DParameter
import org.jetbrains.dokka.model.Documentable
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that, instead of performing transformations,
 * stores the old-new function name pairs into [RenamingsStorage].
 * This should be executed before other transformers that rely on the renamings.
 */
class RenamingsStorer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    private fun storeIfRenamed(documentable: Documentable) {
        val name = getOverriddenName(documentable) ?: return
        RenamingsStorage[documentable.dri] =
            Renaming(
                oldName = requireNotNull(documentable.name),
                newName = name,
            )
    }

    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        storeIfRenamed(function)
        return super.transformFunction(function)
    }

    override fun transformParameter(parameter: DParameter): AnyWithChanges<DParameter> {
        storeIfRenamed(parameter)
        return super.transformParameter(parameter)
    }
}
