package com.quarkdown.core.localization.table

/**
 * The `Language (Territory)` display name format (e.g. `French (Canada)`),
 * owning both directions: composition, used by [TableLocale],
 * and parsing, used by [TableLocaleLoader].
 */
internal object LocaleDisplayName {
    private const val TERRITORY_PREFIX = " ("
    private const val TERRITORY_SUFFIX = ")"

    /**
     * @param language the language name
     * @param territory the optional territory name
     * @return the display name carrying [territory], or just [language] if there is none
     */
    fun format(
        language: String,
        territory: String?,
    ): String =
        when (territory) {
            null -> {
                language
            }

            else -> {
                StringBuilder()
                    .append(language)
                    .append(TERRITORY_PREFIX)
                    .append(territory)
                    .append(TERRITORY_SUFFIX)
                    .toString()
            }
        }

    /**
     * @param name a display name, possibly carrying a territory
     * @return the language name, paired with the territory name if [name] carries one
     */
    fun split(name: String): Pair<String, String?> {
        val carriesTerritory = TERRITORY_PREFIX in name && name.endsWith(TERRITORY_SUFFIX)
        if (!carriesTerritory) return name to null

        val language = name.substringBefore(TERRITORY_PREFIX)
        val territory = name.substringAfter(TERRITORY_PREFIX).removeSuffix(TERRITORY_SUFFIX)
        return language to territory
    }
}
