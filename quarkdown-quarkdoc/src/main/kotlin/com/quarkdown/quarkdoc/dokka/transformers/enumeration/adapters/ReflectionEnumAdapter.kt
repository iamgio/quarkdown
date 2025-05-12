package com.quarkdown.quarkdoc.dokka.transformers.enumeration.adapters

import com.quarkdown.quarkdoc.dokka.transformers.enumeration.QuarkdocEnum
import com.quarkdown.quarkdoc.dokka.transformers.enumeration.QuarkdocEnumEntry
import org.jetbrains.dokka.links.DRI

/**
 * An adapter for a [QuarkdocEnum] that is loaded via reflection.
 * @param cls the enum class
 * @param dri the [DRI] that points to the enum declaration
 */
internal class ReflectionEnumAdapter(
    private val cls: Class<*>,
    private val dri: DRI,
) : QuarkdocEnum {
    override val entries: List<QuarkdocEnumEntry>
        get() =
            cls.enumConstants
                .filterIsInstance<Enum<*>>()
                .map { ReflectionEnumEntryAdapter(it, dri) }
}

/**
 * An adapter for a [QuarkdocEnumEntry] that is loaded via reflection from a [ReflectionEnumAdapter].
 * @param entry the enum entry
 * @param parentDri the [DRI] that points to the enum declaration
 */
internal class ReflectionEnumEntryAdapter(
    private val entry: Enum<*>,
    private val parentDri: DRI,
) : QuarkdocEnumEntry {
    override val name: String
        get() = entry.name

    override val dri: DRI
        get() = parentDri.copy(classNames = parentDri.classNames + '.' + entry.name)
}
