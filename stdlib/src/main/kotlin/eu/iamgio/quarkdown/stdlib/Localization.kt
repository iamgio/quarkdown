package eu.iamgio.quarkdown.stdlib

import eu.iamgio.quarkdown.ast.MarkdownContent
import eu.iamgio.quarkdown.ast.base.TextNode
import eu.iamgio.quarkdown.ast.base.block.ListItem
import eu.iamgio.quarkdown.ast.base.block.Newline
import eu.iamgio.quarkdown.ast.base.block.UnorderedList
import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.context.MutableContext
import eu.iamgio.quarkdown.function.reflect.annotation.Injected
import eu.iamgio.quarkdown.function.reflect.annotation.Name
import eu.iamgio.quarkdown.function.value.StringValue
import eu.iamgio.quarkdown.function.value.VoidValue
import eu.iamgio.quarkdown.function.value.wrappedAsValue
import eu.iamgio.quarkdown.localization.Locale
import eu.iamgio.quarkdown.localization.LocaleLoader
import eu.iamgio.quarkdown.localization.LocalizationEntries
import eu.iamgio.quarkdown.localization.LocalizationTable
import eu.iamgio.quarkdown.util.toPlainText

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
 * @param contents dictionary of locales and their entries as an unordered list,
 *                 in which each item contains the locale name and its entries are key-value pairs in a nested unordered list
 * @throws IllegalArgumentException if the contents are not in the correct format or if the table name is already defined
 */
fun localization(
    @Injected context: MutableContext,
    @Name("name") tableName: String,
    contents: MarkdownContent,
): VoidValue {
    // Duplicate table names are not allowed.
    if (tableName in context.localizationTables) {
        throw IllegalArgumentException("Localization table \"$tableName\" is already defined.")
    }

    // The localization table must be an unordered list.
    val dictionaryList =
        contents.children.singleOrNull { it !is Newline } as? UnorderedList
            ?: throw IllegalArgumentException("Localization table must only contain a list.")

    val table: LocalizationTable =
        dictionaryList.children.asSequence().filterIsInstance<ListItem>().associate { item ->
            // The locale name is the first element of each list item:
            // English <-- this is the locale name
            //   - key1: value1
            //   - key2: value2

            val localeName: String =
                (item.children.firstOrNull() as? TextNode)?.text?.toPlainText()
                    ?: throw IllegalArgumentException("Localization table items must contain a locale name.")

            val locale: Locale =
                LocaleLoader.SYSTEM.fromName(localeName)
                    ?: throw IllegalArgumentException("Could not find locale \"$localeName\".")

            // The second element of each list item is the list of entries:
            // English
            //   - key1: value1 <-- this is the first entry
            //   - key2: value2 <-- this is the second entry

            val entriesList: UnorderedList =
                item.children.getOrNull(1) as? UnorderedList
                    ?: throw IllegalArgumentException("Localization table items must contain a list of entries.")

            // Key-value separator.
            val keyValueSeparator = Regex(":\\s*")

            // Entries are parsed as a map.
            val entries: LocalizationEntries =
                entriesList.children.asSequence()
                    .filterIsInstance<ListItem>()
                    .flatMap { entryItem -> entryItem.children }
                    .filterNot { it is Newline }
                    .associate {
                        // Key and value are extracted.
                        val rawKey =
                            (it as? TextNode)?.text?.toPlainText()
                                ?: throw IllegalArgumentException("Localization table entries must be simple text nodes.")

                        if (keyValueSeparator !in rawKey) {
                            throw IllegalArgumentException("Cannot extract key-value localization pair from \"$rawKey\".")
                        }

                        val (key, value) = rawKey.split(keyValueSeparator, limit = 2)
                        key to value
                    }

            locale to entries
        }

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
