package com.quarkdown.core.context.localization

import com.quarkdown.core.context.Context
import com.quarkdown.core.localization.LocalizationException

/**
 * Name of the stdlib localization table.
 * Not that the library might not always be present,
 * hence it is suggested to use it with [localizeOrNull].
 * @see [com.quarkdown.core.context.Context.localize]
 */
private const val STDLIB_LOCALIZATION_TABLE_NAME = "std"

/**
 * Localizes a key from a table.
 * @param tableName name of the table
 * @param key key to localize
 * @return the localized string if the [key] exists in the table, `null` otherwise
 * @see com.quarkdown.core.context.Context.localize
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
