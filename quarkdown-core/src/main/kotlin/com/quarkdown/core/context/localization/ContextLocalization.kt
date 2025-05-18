package com.quarkdown.core.context.localization

import com.quarkdown.core.context.Context
import com.quarkdown.core.localization.Locale
import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.localization.LocalizationException

/**
 * Name of the stdlib localization table.
 * Not that the library might not always be present,
 * hence it is suggested to use it with [localizeOrNull].
 * @see [com.quarkdown.core.context.Context.localize]
 */
private const val STDLIB_LOCALIZATION_TABLE_NAME = "std"

/**
 * Default locale to use as fallback via [localizeOrDefault] if a localization key is not found.
 * Defaults to English (en).
 */
private val DEFAULT_LOCALE by lazy { LocaleLoader.SYSTEM.fromTag("en") }

/**
 * Localizes a key from a table.
 * @param key key to localize
 * @param tableName name of the table. Defaults to the stdlib table (`std`).
 * @param locale the locale to localize for, defaulting to the one set in the context's metadata, if any
 * @return the localized string if the [key] exists in the table, `null` otherwise
 * @see com.quarkdown.core.context.Context.localize
 */
fun Context.localizeOrNull(
    tableName: String = STDLIB_LOCALIZATION_TABLE_NAME,
    key: String,
    locale: Locale? = null,
): String? =
    try {
        when (locale) {
            null -> localize(tableName, key)
            else -> localize(tableName, key, locale)
        }
    } catch (e: LocalizationException) {
        null
    }

/**
 * Localizes a key from the stdlib table. If the key is not found in the context's locale,
 * it falls back to the default locale (English).
 * @param tableName name of the table. Defaults to the stdlib table (`std`).
 * @param key localization key
 * @return the localized string (preferably in the context's locale, or in the fallback locale otherwise)
 * if the [key] exists in the `std` table, `null` otherwise
 */
fun Context.localizeOrDefault(
    tableName: String = STDLIB_LOCALIZATION_TABLE_NAME,
    key: String,
): String? =
    localizeOrNull(tableName, key)
        ?: localizeOrNull(tableName, key, locale = DEFAULT_LOCALE)
