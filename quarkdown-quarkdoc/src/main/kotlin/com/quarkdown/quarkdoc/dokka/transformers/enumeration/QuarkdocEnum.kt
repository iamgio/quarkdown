package com.quarkdown.quarkdoc.dokka.transformers.enumeration

import org.jetbrains.dokka.links.DRI

/**
 * An enum that can be represented and listed in documentation.
 */
interface QuarkdocEnum {
    /**
     * The ordered entries of the enum.
     */
    val entries: List<QuarkdocEnumEntry>
}

/**
 * An entry of an enum that can be represented in documentation.
 */
interface QuarkdocEnumEntry {
    /**
     * The name of the entry.
     */
    val name: String

    /**
     * The [DRI] to the entry definition.
     */
    val dri: DRI
}
