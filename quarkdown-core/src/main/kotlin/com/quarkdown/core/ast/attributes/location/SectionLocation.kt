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
)
