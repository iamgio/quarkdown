package eu.iamgio.quarkdown.document.locale

/**
 * Represents a locale, which defines the document language.
 */
interface Locale {
    /**
     * Language code of the locale.
     * For instance, `en` for English and `it` for Italian.
     */
    val code: String

    /**
     * Country code of the locale.
     */
    val countryCode: String?

    /**
     * Name of the locale, possibly in the locale's language itself.
     * For instance, `English` for English and `italiano` for Italian.
     */
    val localizedName: String

    /**
     * Name of the country of the locale, possibly in the locale's language itself.
     * For instance, `United States` for `en-US` and `Italia` for `it`.
     */
    val localizedCountryName: String?

    /**
     * Tag of the locale.
     * For instance, `en-US` for US English and `it` for Italian.
     */
    val tag: String
}
