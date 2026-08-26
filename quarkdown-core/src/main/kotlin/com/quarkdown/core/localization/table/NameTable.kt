package com.quarkdown.core.localization.table

/**
 * An index over a code-to-English-name table stored as sorted parallel lists,
 * with instances emitted at build time by the `quarkdown-locale-table-processor` KSP processor.
 * @param codes the table codes, sorted
 * @param names the English names, parallel to [codes]
 */
internal class NameTable(
    val codes: List<String>,
    private val names: List<String>,
) {
    /**
     * @return whether [code] is present in this table
     */
    operator fun contains(code: String): Boolean = codes.binarySearch(code) >= 0

    /**
     * @param code the code to look up
     * @return the English name for [code], or `null` if absent
     */
    fun nameOf(code: String): String? = names.getOrNull(codes.binarySearch(code))

    /**
     * @param name the English name to look up, case-insensitively
     * @return the code whose name matches [name], or `null` if absent
     */
    fun codeOf(name: String): String? = codes.getOrNull(names.indexOfFirst { it.equals(name, ignoreCase = true) })
}
