package com.quarkdown.quarkdoc.dokka.transformers

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.plugability.DokkaContext

/**
 * Transformer that, instead of performing transformations,
 * stores the old-new function name pairs into [RenamingsStorage].
 * This should be executed before other transformers that rely on the renamings.
 */
class RenamingsStorer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        getOverriddenName(function)?.let {
            PrivateRenamingsStorage.renamings[function.dri] = it
        }
        return super.transformFunction(function)
    }
}

private object PrivateRenamingsStorage {
    /**
     * Renamed function names associated with their address.
     */
    val renamings: MutableMap<DRI, String> = mutableMapOf()
}

/**
 * Storage for the old-new function name pairs.
 * This is a mutable map that is populated by the [RenamingsStorer] transformer.
 */
object RenamingsStorage {
    /**
     * @return the new name for the function with the given DRI, or null if it is not found.
     */
    operator fun get(dri: DRI): String? = PrivateRenamingsStorage.renamings[dri]
}
