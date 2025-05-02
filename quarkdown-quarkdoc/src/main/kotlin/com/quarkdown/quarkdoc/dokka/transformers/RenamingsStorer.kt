package com.quarkdown.quarkdoc.dokka.transformers

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DFunction
import org.jetbrains.dokka.plugability.DokkaContext

private val mutableRenamings = mutableMapOf<DRI, String>()

/**
 * Transformer that, instead of performing transformations,
 * stores the old-new function name pairs into [RenamingsStorage].
 * This should be executed before other transformers that rely on the renamings.
 */
class RenamingsStorer(
    context: DokkaContext,
) : QuarkdocDocumentableReplacerTransformer(context) {
    override fun transformFunction(function: DFunction): AnyWithChanges<DFunction> {
        getOverriddenName(function)?.let { mutableRenamings[function.dri] = it }
        return super.transformFunction(function)
    }
}

/**
 * Storage for the old-new function name pairs.
 * This is a mutable map that is populated by the [RenamingsStorer] transformer.
 */
object RenamingsStorage {
    /**
     * Renamed function names associated with their address.
     */
    val functionRenamings: Map<DRI, String>
        get() = mutableRenamings
}
