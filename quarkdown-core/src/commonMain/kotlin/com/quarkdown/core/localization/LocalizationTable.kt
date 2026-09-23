package com.quarkdown.core.localization

/**
 * A collection of multiple [LocalizationTable]s, each defined by its own unique name.
 * @see com.quarkdown.core.context.Context.localizationTables
 */
typealias LocalizationTables = Map<String, LocalizationTable>

/**
 * A mutable variant of [LocalizationTables].
 */
typealias MutableLocalizationTables = MutableMap<String, LocalizationTable>

/**
 * A table that enables localization by storing localization entries for specific [Locale]s.
 * @see com.quarkdown.core.context.Context.localize
 */
typealias LocalizationTable = Map<Locale, LocalizationEntries>

/**
 * Key-value pairs that define the localization associated with that key for a specific [Locale].
 */
typealias LocalizationEntries = Map<String, String>
