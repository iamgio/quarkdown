package com.quarkdown.quarkdoc.dokka.transformers.name

import org.jetbrains.dokka.links.DRI

/**
 * A renaming of a function or parameter via the `@Name` annotation.
 * @property oldName the original name
 * @property newName the new name
 */
data class Renaming(
    val oldName: String,
    val newName: String,
)

/**
 * Storage for the old-new function name pairs.
 * This is a mutable map that is populated by the [RenamingsStorer] transformer.
 */
object RenamingsStorage {
    private val renamings: MutableMap<DRI, Renaming> = mutableMapOf()

    /**
     * @return the new name for the function with the given DRI, or null if it is not found.
     */
    operator fun get(dri: DRI): Renaming? = renamings[dri]

    /**
     * Updates the renaming for the given address.
     */
    operator fun set(
        dri: DRI,
        renaming: Renaming,
    ) {
        renamings[dri] = renaming
    }

    /**
     * Clears the stored renamings.
     */
    fun clear() {
        renamings.clear()
    }
}
