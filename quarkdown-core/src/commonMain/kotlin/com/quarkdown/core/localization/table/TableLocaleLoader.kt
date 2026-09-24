package com.quarkdown.core.localization.table

import com.quarkdown.core.localization.Locale
import com.quarkdown.core.localization.LocaleLoader

/**
 * [LocaleLoader] backed by the bundled, compile-time-generated,
 * `Languages` and `Territories` tables.
 */
internal object TableLocaleLoader : LocaleLoader {
    override val all: Sequence<Locale>
        get() = Languages.codes.asSequence().map { TableLocale(it, countryCode = null) }

    override fun fromTag(tag: String): Locale? {
        val subtags = tag.split('-')

        val language = subtags.first().lowercase()
        if (language !in Languages) return null

        // The first subtag that is a known territory is the region.
        val country = subtags.drop(1).map(String::uppercase).firstOrNull { it in Territories }
        return TableLocale(language, country)
    }

    override fun fromName(name: String): Locale? {
        val (languageName, territoryName) = LocaleDisplayName.split(name.trim())

        val language = Languages.codeOf(languageName) ?: return null
        val country = territoryName?.let { Territories.codeOf(it) ?: return null }
        return TableLocale(language, country)
    }
}
