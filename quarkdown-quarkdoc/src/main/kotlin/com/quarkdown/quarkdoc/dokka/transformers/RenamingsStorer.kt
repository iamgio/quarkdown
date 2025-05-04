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
            PrivateRenamingsStorage.renamings[function.dri] =
                Renaming(
                    oldName = function.name,
                    newName = it,
                )
        }
        function.parameters.forEach { parameter ->
            getOverriddenName(parameter)?.let {
                PrivateRenamingsStorage.renamings[parameter.dri] =
                    Renaming(
                        oldName = parameter.name!!,
                        newName = it,
                    )
            }
        }
        return super.transformFunction(function)
    }
}

data class Renaming(
    val oldName: String,
    val newName: String,
)

private object PrivateRenamingsStorage {
    /**
     * Renamed function names associated with their address.
     */
    val renamings: MutableMap<DRI, Renaming> = mutableMapOf()
}

/**
 * Storage for the old-new function name pairs.
 * This is a mutable map that is populated by the [RenamingsStorer] transformer.
 */
object RenamingsStorage {
    /**
     * @return the new name for the function with the given DRI, or null if it is not found.
     */
    operator fun get(dri: DRI): Renaming? = PrivateRenamingsStorage.renamings[dri]

    /**
     * Clears the stored renamings.
     */
    fun clear() {
        PrivateRenamingsStorage.renamings.clear()
    }
}
