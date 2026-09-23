package com.quarkdown.core.ast.attributes.location

/**
 * The location of a node within the document, in terms of section indices.
 * Example:
 * ```markdown
 * # A
 * ## A.A
 * # B
 * ## B.A
 * Node <-- location: B.A, represented by the levels [2, 1]
 * ```
 * @param levels section indices
 */
data class SectionLocation(
    val levels: List<Int>,
) {
    /**
     * The depth of this location, i.e., the number of levels it contains.
     * Example: the location `[1, 1]` has a depth of `2`.
     *
     * This is related to [com.quarkdown.core.document.numbering.NumberingFormat.accuracy]
     * in order to determine whether a location can be used as a label.
     */
    val depth: Int
        get() = levels.size
}
