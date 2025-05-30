package com.quarkdown.quarkdoc.dokka.transformers.enumeration

import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DEnum

/**
 * Storage of enum declarations.
 * @see EnumStorer
 * @see EnumParameterEntryListerTransformer
 */
object EnumStorage {
    private val enums = mutableSetOf<DEnum>()

    /**
     * Registers an enum declaration.
     */
    operator fun plusAssign(enum: DEnum) {
        enums += enum
    }

    /**
     * @return the enum declaration associated with the given [dri], if any
     */
    fun fromDRI(dri: DRI) = enums.find { it.dri == dri }

    /**
     * Removes all enum declarations from this storage.
     */
    fun clear() {
        enums.clear()
    }
}
