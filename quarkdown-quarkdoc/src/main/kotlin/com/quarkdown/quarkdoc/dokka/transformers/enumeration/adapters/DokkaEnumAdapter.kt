package com.quarkdown.quarkdoc.dokka.transformers.enumeration.adapters

import com.quarkdown.quarkdoc.dokka.transformers.enumeration.QuarkdocEnum
import com.quarkdown.quarkdoc.dokka.transformers.enumeration.QuarkdocEnumEntry
import org.jetbrains.dokka.links.DRI
import org.jetbrains.dokka.model.DEnum
import org.jetbrains.dokka.model.DEnumEntry

/**
 * An adapter for a [QuarkdocEnum] from a Dokka-loaded enum.
 * @param enum the enum model
 */
internal class DokkaEnumAdapter(
    private val enum: DEnum,
) : QuarkdocEnum {
    override val entries: List<QuarkdocEnumEntry>
        get() = enum.entries.map(::DokkaEnumEntryAdapter)
}

/**
 * An adapter for a [QuarkdocEnumEntry] from a Dokka-loaded enum entry.
 * @param entry the enum entry model
 */
internal class DokkaEnumEntryAdapter(
    private val entry: DEnumEntry,
) : QuarkdocEnumEntry {
    override val name: String
        get() = entry.name

    override val dri: DRI
        get() = entry.dri
}
