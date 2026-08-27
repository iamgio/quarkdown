package com.quarkdown.core.localization

import com.quarkdown.core.localization.table.TableLocaleLoader

/**
 * Loader of [Locale]s.
 */
interface LocaleLoader {
    /**
     * All supported base-language locales, without territory variants.
     * Territory-carrying locales (e.g. `en-US`) are resolved on demand
     * via [fromTag] or [fromName].
     */
    val all: Sequence<Locale>

    /**
     * @param tag language code of the locale and optionally the country code, separated by a hyphen.
     *            Example: `en`, `en-US`, `it`, `fr-CA`
     * @return [Locale] with the given tag, or `null` if not found
     */
    fun fromTag(tag: String): Locale?

    /**
     * @param name English name of the locale.
     *             Example: `English`, `Italian`, `French`
     * @return [Locale] with the given name, or `null` if not found
     */
    fun fromName(name: String): Locale?

    /**
     * Finds a locale by its tag or name.
     * @param identifier tag (`en`, `it`, `fr-CA`) or English name (`English`, `Italian`, `French (Canada)`) of the locale
     * @return [Locale] with the given tag or name, or `null` if not found
     */
    fun find(identifier: String): Locale? = fromName(identifier) ?: fromTag(identifier)

    companion object {
        /**
         * Default [LocaleLoader] implementation, backed by bundled locale name tables
         * for platform-independent, deterministic results.
         */
        val SYSTEM: LocaleLoader
            get() = TableLocaleLoader
    }
}
