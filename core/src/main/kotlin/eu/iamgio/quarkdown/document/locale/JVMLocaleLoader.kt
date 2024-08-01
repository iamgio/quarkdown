package eu.iamgio.quarkdown.document.locale

/**
 * Loader of [JVMLocale]s.
 */
internal object JVMLocaleLoader : LocaleLoader {
    override val all: Iterable<Locale>
        get() = java.util.Locale.getAvailableLocales().map(::JVMLocale)

    override fun fromTag(tag: String): Locale? =
        java.util.Locale.forLanguageTag(tag)
            ?.let(::JVMLocale)
            ?.takeIf { it.code.isNotBlank() }
}
