package com.quarkdown.processor.locale

import java.util.Locale

internal const val PACKAGE_NAME = "com.quarkdown.core.localization.table"
internal const val FILE_NAME = "LocaleTables"

/**
 * Builder of the generated locale tables Kotlin source: two `NameTable` instances,
 * `Languages` and `Territories`, whose data is extracted from the build JDK's CLDR data via [Locale].
 *
 * Languages are the union of the ISO 639 codes and the languages of the JDK's available locales
 * (which adds three-letter codes such as `yue`).
 */
internal class LocaleTableCodeGenerator {
    /**
     * A table to generate.
     * @param doc the KDoc sentence of the emitted property
     * @param propertyName the name of the emitted property
     * @param extract producer of the table's code-to-English-name data
     */
    private class GeneratedTable(
        val doc: String,
        val propertyName: String,
        val extract: () -> Map<String, String>,
    )

    private val tables =
        listOf(
            GeneratedTable("Languages by ISO 639 code", "Languages", ::languages),
            GeneratedTable("Territories by ISO 3166 country code", "Territories", ::territories),
        )

    /**
     * @param display resolver of a code's English display name
     * @return [this] codes associated with their English name,
     *         skipping codes without one (blank, or echoing the code itself)
     */
    private fun Set<String>.namesBy(display: (String) -> String): Map<String, String> =
        associateWith(display).filterNot { (code, name) -> name.isBlank() || name == code }

    /**
     * @return language codes associated with their English name
     */
    @Suppress("DEPRECATION") // Locale(String) preserves legacy codes (e.g. `iw`) that forLanguageTag would normalize.
    private fun languages(): Map<String, String> {
        val codes = Locale.getISOLanguages().toSet() + Locale.getAvailableLocales().mapNotNull { it.language.takeIf(String::isNotBlank) }
        return codes.namesBy { Locale(it).getDisplayLanguage(Locale.ENGLISH) }
    }

    /**
     * @return country codes associated with their English territory name
     */
    private fun territories(): Map<String, String> =
        Locale.getISOCountries().toSet().namesBy {
            Locale
                .Builder()
                .setRegion(it)
                .build()
                .getDisplayCountry(Locale.ENGLISH)
        }

    private fun String.quoted() = "\"$this\""

    /**
     * @return a multiline `listOf` literal of [values], one entry per line, at column zero
     */
    private fun listLiteral(values: Collection<String>): String =
        values.joinToString(separator = "\n", prefix = "listOf(\n", postfix = "\n)") { "    ${it.quoted()}," }

    /**
     * @return the source of [table]'s emitted `internal val` declaration,
     *         with codes sorted as required by `NameTable`'s binary search
     */
    private fun tableSource(table: GeneratedTable): String {
        val data = table.extract().toSortedMap()
        val arguments =
            listOf("codes" to data.keys, "names" to data.values).joinToString("\n") { (parameter, values) ->
                "$parameter =\n${listLiteral(values).prependIndent("    ")},"
            }
        return buildString {
            appendLine("/**")
            appendLine(" * ${table.doc}.")
            appendLine(" */")
            appendLine("internal val ${table.propertyName}: NameTable =")
            appendLine("    NameTable(")
            appendLine(arguments.prependIndent("        "))
            append("    )")
        }
    }

    /**
     * @return the complete generated source file
     */
    fun buildSource(): String = tables.joinToString(separator = "\n\n", postfix = "\n", prefix = header()) { tableSource(it) }

    private fun header(): String =
        buildString {
            appendLine("// Generated at build time by the `quarkdown-locale-table-processor` KSP processor. Do not edit.")
            appendLine("package $PACKAGE_NAME")
            appendLine()
        }
}
