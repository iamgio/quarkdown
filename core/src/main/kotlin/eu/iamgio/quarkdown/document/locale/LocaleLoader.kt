package eu.iamgio.quarkdown.document.locale

/**
 * Loader of [Locale]s.
 */
interface LocaleLoader {
    /**
     * All available locales.
     */
    val all: Iterable<Locale>

    /**
     * @param tag language code of the locale and optionally the country code, separated by a hyphen.
     *            Example: `en`, `en-US`, `it`, `fr-CA`
     * @return [Locale] with the given tag, or `null` if not found
     */
    fun fromTag(tag: String): Locale?
}
