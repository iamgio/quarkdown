package com.quarkdown.core.localization

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
     * Name of the locale, localized in English.
     * For instance, `English` for English and `Italian` for Italian.
     */
    val displayName: String

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

    /**
     * Short tag of the locale.
     * For instance, `en` for English and `it` for Italian.
     */
    val shortTag: String
}

/**
 * The set of CJK (Chinese, Japanese, Korean) language codes.
 */
private val CJK_LANGUAGE_CODES = setOf("zh", "ja", "ko", "yue", "cmn", "wuu", "hak")

/**
 * Returns `true` if this locale's language is a CJK (Chinese, Japanese, Korean) language,
 * where soft line breaks within paragraphs should not insert a space.
 * Returns `false` if the locale is null.
 */
fun Locale?.isCJK(): Boolean = this != null && (code.lowercase() in CJK_LANGUAGE_CODES || shortTag.lowercase() in CJK_LANGUAGE_CODES)
