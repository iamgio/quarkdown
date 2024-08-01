package eu.iamgio.quarkdown.document.locale

/**
 * [Locale] implementation using [java.util.Locale].
 */
internal class JVMLocale(private val jvmLocale: java.util.Locale) : Locale {
    override val code: String
        get() = jvmLocale.language

    override val countryCode: String?
        get() = jvmLocale.country.takeIf { it.isNotBlank() }

    override val localizedName: String
        get() = jvmLocale.getDisplayLanguage(jvmLocale)

    override val localizedCountryName: String?
        get() = jvmLocale.getDisplayCountry(jvmLocale).takeIf { it.isNotBlank() }
}
