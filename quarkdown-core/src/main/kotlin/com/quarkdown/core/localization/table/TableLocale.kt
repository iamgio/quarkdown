package com.quarkdown.core.localization.table

import com.quarkdown.core.localization.Locale

/**
 * [Locale] implementation backed by the bundled, compile-time-generated,
 * `Languages` and `Territories` tables.
 *
 * Instances are created by [TableLocaleLoader], which guarantees that [code]
 * and [countryCode] are present in the tables.
 */
internal data class TableLocale(
    override val code: String,
    override val countryCode: String?,
) : Locale {
    override val displayName: String
        get() =
            LocaleDisplayName.format(
                language = checkNotNull(Languages.nameOf(code)),
                territory = countryCode?.let { checkNotNull(Territories.nameOf(it)) },
            )

    override val tag: String
        get() = countryCode?.let { "$code-$it" } ?: code

    override val shortTag: String
        get() = code
}
