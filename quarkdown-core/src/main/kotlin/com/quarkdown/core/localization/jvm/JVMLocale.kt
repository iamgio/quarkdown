package com.quarkdown.core.localization.jvm

import com.quarkdown.core.localization.Locale

/**
 * [Locale] implementation using [java.util.Locale].
 */
internal data class JVMLocale(
    private val jvmLocale: JLocale,
) : Locale {
    override val code: String
        get() = jvmLocale.language

    override val countryCode: String?
        get() = jvmLocale.country.takeIf { it.isNotBlank() }

    override val displayName: String
        get() = jvmLocale.getDisplayName(JLocale.ENGLISH)

    override val localizedName: String
        get() = jvmLocale.getDisplayName(jvmLocale)

    override val localizedCountryName: String?
        get() = jvmLocale.getDisplayCountry(jvmLocale).takeIf { it.isNotBlank() }

    override val tag: String
        get() = jvmLocale.toLanguageTag()
}
