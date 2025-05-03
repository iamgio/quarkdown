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
            PrivateRenamingsStorage.addFunctionRenaming(function, it)
        }
        return super.transformFunction(function)
    }
}

private object PrivateRenamingsStorage {
    /**
     * Renamed function names associated with their address.
     */
    val functionRenamingsByAddress: MutableMap<DRI, String> = mutableMapOf()

    /**
     * Renamed function names associated with their old name.
     */
    val functionRenamingsByOldName: MutableMap<String, String> = mutableMapOf()

    /**
     * Adds a function renaming to the storage.
     */
    fun addFunctionRenaming(
        function: DFunction,
        newName: String,
    ) {
        functionRenamingsByAddress[function.dri] = newName
        functionRenamingsByOldName[function.name] = newName
    }
}

/**
 * Storage for the old-new function name pairs.
 * This is a mutable map that is populated by the [RenamingsStorer] transformer.
 */
object RenamingsStorage {
    fun getFunctionRenamingByAddress(dri: DRI): String? = PrivateRenamingsStorage.functionRenamingsByAddress[dri]

    fun getFunctionRenamingByOldName(oldName: String): String? = PrivateRenamingsStorage.functionRenamingsByOldName[oldName]
}
