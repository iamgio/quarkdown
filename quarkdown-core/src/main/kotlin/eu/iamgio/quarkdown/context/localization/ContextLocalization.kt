package eu.iamgio.quarkdown.context.localization

import eu.iamgio.quarkdown.context.Context
import eu.iamgio.quarkdown.localization.LocalizationException

/**
 * Name of the stdlib localization table.
 * Not that the library might not always be present,
 * hence it is suggested to use it with [localizeOrNull].
 * @see [eu.iamgio.quarkdown.context.Context.localize]
 */
private const val STDLIB_LOCALIZATION_TABLE_NAME = "std"

/**
 * Localizes a key from a table.
 * @param tableName name of the table
 * @param key key to localize
 * @return the localized string if the [key] exists in the table, `null` otherwise
 * @see eu.iamgio.quarkdown.context.Context.localize
 */
fun Context.localizeOrNull(
    tableName: String,
    key: String,
): String? =
    try {
        localize(tableName, key)
    } catch (e: LocalizationException) {
        null
    }

/**
 * Localizes a key from the stdlib table.
 * @param key key to localize
 * @return the localized string if the [key] exists in the `std` table, `null` otherwise
 * @see localizeOrNull
 */
fun Context.localizeOrNull(key: String): String? = localizeOrNull(STDLIB_LOCALIZATION_TABLE_NAME, key)
