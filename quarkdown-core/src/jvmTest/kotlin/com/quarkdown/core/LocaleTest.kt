package com.quarkdown.core

import com.quarkdown.core.localization.LocaleLoader
import com.quarkdown.core.localization.isCJK
import com.quarkdown.core.localization.table.TableLocaleLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for locale retrievals.
 * @see LocalizationTest
 */
class LocaleTest {
    private val retriever = TableLocaleLoader

    @Test
    fun `default retriever`() {
        assertEquals(retriever, LocaleLoader.SYSTEM)
    }

    @Test
    fun english() {
        with(retriever.fromTag("en")) {
            assertNotNull(this)
            assertEquals(this, retriever.fromName("English"))
            assertEquals(this, retriever.find("English"))
            assertEquals(this, retriever.find("eNgLiSh"))
            assertEquals("en", code)
            assertEquals("en", tag)
            assertEquals("en", shortTag)
            assertEquals("English", displayName)
            assertNull(countryCode)
        }
    }

    @Test
    fun italian() {
        with(retriever.find("it")) {
            assertNotNull(this)
            assertEquals(this, retriever.fromName("Italian"))
            assertEquals(this, retriever.find("Italian"))
            assertEquals(this, retriever.find("iTaLiAn"))
            assertEquals("it", code)
            assertEquals("it", tag)
            assertEquals("it", shortTag)
            assertEquals("Italian", displayName)
            assertNull(countryCode)
        }
    }

    @Test
    fun `english-us`() {
        with(retriever.find("en-US")) {
            assertNotNull(this)
            assertEquals(this, retriever.find("English (United States)"))
            assertEquals(this, retriever.find("En-us"))
            assertEquals("en", code)
            assertEquals("en-US", tag)
            assertEquals("en", shortTag)
            assertEquals("English (United States)", displayName)
            assertEquals("US", countryCode)
        }
    }

    @Test
    fun `french-canada`() {
        with(retriever.find("fr-CA")) {
            assertNotNull(this)
            assertEquals(this, retriever.find("French (Canada)"))
            assertEquals("fr", code)
            assertEquals("fr-CA", tag)
            assertEquals("fr", shortTag)
            assertEquals("French (Canada)", displayName)
            assertEquals("CA", countryCode)
        }
    }

    @Test
    fun `territory name containing parentheses`() {
        with(retriever.find("en-CC")) {
            assertNotNull(this)
            assertEquals("English (Cocos (Keeling) Islands)", displayName)
            // Round-trip: the display name resolves back to the same locale.
            assertEquals(this, retriever.fromName(displayName))
        }
    }

    @Test
    fun cjk() {
        with(retriever.find("zh")) {
            assertNotNull(this)
            assertTrue(isCJK())
        }
        with(retriever.find("Chinese")) {
            assertNotNull(this)
            assertTrue(isCJK())
        }
        with(retriever.find("ja")) {
            assertNotNull(this)
            assertTrue(isCJK())
        }
        with(retriever.find("ko")) {
            assertNotNull(this)
            assertTrue(isCJK())
        }
    }

    @Test
    fun invalid() {
        assertNull(retriever.fromTag("nonexistent"))
        assertNull(retriever.fromName("nonexistent"))
        assertNull(retriever.find("nonexistent"))
    }

    @Test
    fun `all locales are loaded`() {
        assertTrue(retriever.all.iterator().hasNext())
    }
}
