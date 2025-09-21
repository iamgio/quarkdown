package com.quarkdown.core.ast.attributes.localization

/**
 * Keys for localization of kinds of nodes,
 * used to look up localized strings in the default [com.quarkdown.core.localization.LocalizationTable].
 * @see LocalizedKind
 */
object LocalizedKindKeys {
    /**
     * @see com.quarkdown.core.ast.quarkdown.block.Figure
     */
    const val FIGURE = "figure"

    /**
     * @see com.quarkdown.core.ast.base.block.Heading
     */
    const val HEADING = "section"

    /**
     * @see com.quarkdown.core.ast.base.block.Table
     */
    const val TABLE = "table"
}
