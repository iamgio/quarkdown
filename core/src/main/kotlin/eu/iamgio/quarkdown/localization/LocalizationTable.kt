package eu.iamgio.quarkdown.localization

/**
 * A collection of multiple [LocalizationTable]s, each defined by its own unique name.
 * @see eu.iamgio.quarkdown.context.Context.localizationTables
 */
typealias LocalizationTables = Map<String, LocalizationTable>

/**
 * A table that enables localization by storing localization entries for specific [Locale]s.
 * @see eu.iamgio.quarkdown.context.Context.localize
 */
typealias LocalizationTable = Map<Locale, LocalizationEntries>

/**
 * Key-value pairs that define the localization associated with that key for a specific [Locale].
 */
typealias LocalizationEntries = Map<String, String>
