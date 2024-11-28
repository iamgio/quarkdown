package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.reflect.annotation.Injected
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.DictionaryValue
import eu.iamgio.quarkdown.function.value.OutputValue
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.localization.Locale
import eu.iamgio.quarkdown.localization.LocaleLoader
import eu.iamgio.quarkdown.localization.LocalizationEntries

/**
 * `Localization` stdlib module exporter.
 * This module handles localization-related features.
 */
val Localization: Module =
    setOf(
        ::localization,
        ::localize,
    )

/**
 * Defines and registers a new localization table, whose entries are key-value pairs for each locale and defined by a Markdown dictionary.
 *
 * Example:
 * ```
 * .localization
 *     - English
 *         - morning: Good morning
 *         - evening: Good evening
 *     - Italian
 *         - morning: Buongiorno
 *         - evening: Buonasera
 * ```
 *
 * @param tableName name of the localization table. Must be unique
 * @param contents dictionary of locales and their key-value entries
 * @throws IllegalArgumentException if the contents are not in the correct format or if the table name is already defined
 */
fun localization(
    @Injected context: MutableContext,
    @Name("name") tableName: String,
    contents: Map<String, DictionaryValue<OutputValue<String>>>,
): VoidValue {
    // Duplicate table names are not allowed.
    if (tableName in context.localizationTables) {
        throw IllegalArgumentException("Localization table \"$tableName\" is already defined.")
    }

    val table =
        contents.asSequence().map { (key, value) ->
            // The locale name is the first element of each list item:
            // English <-- this is the locale name
            //   - key1: value1
            //   - key2: value2
            val locale: Locale =
                LocaleLoader.SYSTEM.fromName(key)
                    ?: throw IllegalArgumentException("Could not find locale \"${key}\".")

            val entries: LocalizationEntries =
                value.unwrappedValue.mapValues { (_, value) -> value.unwrappedValue }

            locale to entries
        }.toMap()

    // The table is registered to the context.
    context.localizationTables[tableName] = table

    return VoidValue
}

/**
 * Localizes a key from a pre-existing localization table (defined via [localization]).
 *
 * Example:
 * ```
 * .localize("mytable:key")
 * ```
 *
 * @param key key to localize, in the format `tableName:keyName`
 * @param separator separator between the table name and the key name. Defaults to `:`
 * @return the localized value
 * @throws eu.iamgio.quarkdown.localization.LocalizationException if an error occurs during the lookup
 * @see localization
 */
fun localize(
    @Injected context: Context,
    key: String,
    separator: String = ":",
): StringValue {
    val (tableName, keyName) = key.split(separator, limit = 2)
    return context.localize(tableName, keyName).wrappedAsValue()
}
