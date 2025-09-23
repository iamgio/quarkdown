package com.quarkdown.core

import com.quarkdown.core.context.Context
import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.localization.localizeOrDefault
import com.quarkdown.core.context.localization.localizeOrNull
import com.quarkdown.core.document.DocumentInfo
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.localization.Locale
import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.localization.LocaleNotSetException
import com.quarkdown.core.localization.LocalizationKeyNotFoundException
import com.quarkdown.core.localization.LocalizationLocaleNotFoundException
import com.quarkdown.core.localization.LocalizationTable
import com.quarkdown.core.localization.LocalizationTableNotFoundException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * Tests for localization.
 * @see LocaleTest
 */
class LocalizationTest {
    private val loader = LocaleLoader.SYSTEM

    private fun context(locale: Locale?): Context {
        val table: LocalizationTable =
            mapOf(
                loader.fromName("English")!! to
                    mapOf(
                        "morning" to "Good morning",
                        "evening" to "Good evening",
                    ),
                loader.fromName("Italian")!! to
                    mapOf(
                        "morning" to "Buongiorno",
                        "evening" to "Buonasera",
                    ),
            )

        return MutableContext(QuarkdownFlavor).apply {
            documentInfo = DocumentInfo(locale = locale)
            localizationTables["mytable"] = table
        }
    }

    @Test
    fun `locale not set`() {
        val context = context(null)
        assertFailsWith<LocaleNotSetException> { context.localize("mytable", "morning") }
    }

    @Test
    fun `english localization`() {
        val context = context(loader.fromName("English")!!)
        assertEquals("Good morning", context.localize("mytable", "morning"))
        assertEquals("Good evening", context.localize("mytable", "evening"))
    }

    @Test
    fun `italian localization`() {
        val context = context(loader.fromName("Italian")!!)
        assertEquals("Buongiorno", context.localize("mytable", "morning"))
        assertEquals("Buonasera", context.localize("mytable", "evening"))
    }

    @Test
    fun `invalid key`() {
        val context = context(loader.fromName("English")!!)
        assertFailsWith<LocalizationKeyNotFoundException> { context.localize("mytable", "afternoon") }
        assertFailsWith<LocalizationTableNotFoundException> { context.localize("sometable", "morning") }
    }

    @Test
    fun `invalid locale`() {
        val context = context(loader.fromName("French")!!)
        assertFailsWith<LocalizationLocaleNotFoundException> { context.localize("mytable", "morning") }
    }

    @Test
    fun `invalid locale with null`() {
        val context = context(loader.fromName("French")!!)
        assertNull(context.localizeOrNull("mytable", "morning"))
    }

    @Test
    fun `invalid locale with default`() {
        val context = context(loader.fromName("French")!!)
        assertEquals("Good morning", context.localizeOrDefault("mytable", "morning"))
    }
}
