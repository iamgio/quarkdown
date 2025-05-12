package com.quarkdown.quarkdoc.dokka.transformers.enumeration.adapters

import com.quarkdown.quarkdoc.dokka.transformers.enumeration.EnumStorage
import com.quarkdown.quarkdoc.dokka.transformers.enumeration.QuarkdocEnum
import com.quarkdown.quarkdoc.dokka.util.fullyQualifiedReflectionName
import org.jetbrains.dokka.links.DRI

/**
 * Utilities to adapt a [QuarkdocEnum] from different sources.
 */
object QuarkdocEnumAdapters {
    /**
     * Looks up a [QuarkdocEnum] from the given [DRI].
     * - If the enum is declared in the same module, it will be found in the [EnumStorage] as a [DokkaEnumAdapter].
     * - If the enum is declared in a different module that is present in this classpath (e.g. `core`),
     *   it will be loaded via reflection as a [ReflectionEnumAdapter].
     * @param dri the [DRI] that points to the enum declaration.
     * @return a [QuarkdocEnum] from the given [DRI], or `null` if it cannot be found.
     */
    fun fromDRI(dri: DRI): QuarkdocEnum? =
        EnumStorage.fromDRI(dri)?.let(::DokkaEnumAdapter)
            ?: try {
                Class
                    .forName(dri.fullyQualifiedReflectionName)
                    .takeIf { it.isEnum }
                    ?.let { ReflectionEnumAdapter(it, dri) }
            } catch (_: Exception) {
                null
            }
}
