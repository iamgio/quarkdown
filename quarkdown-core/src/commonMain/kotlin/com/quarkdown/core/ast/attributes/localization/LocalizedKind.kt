package com.quarkdown.core.ast.attributes.localization

/**
 * A node whose kind, e.g. "figure", "table", can be localized to the document language.
 */
interface LocalizedKind {
    /**
     * Key for localization of the kind of this node,
     * used to look up localized strings in the default [com.quarkdown.core.localization.LocalizationTable].
     * @see LocalizedKindKeys
     */
    val kindLocalizationKey: String
}
