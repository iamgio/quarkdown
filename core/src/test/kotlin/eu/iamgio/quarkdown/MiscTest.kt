package eu.iamgio.quarkdown

import eu.iamgio.quarkdown.document.locale.JVMLocaleLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for miscellaneous classes.
 */
class MiscTest {
    @Test
    fun locale() {
        val retriever = JVMLocaleLoader

        with(retriever.fromTag("en")) {
            assertNotNull(this)
            assertEquals("en", code)
            assertEquals("en", tag)
            assertEquals("English", localizedName)
            assertNull(countryCode)
            assertNull(localizedCountryName)
        }

        with(retriever.fromTag("it")) {
            assertNotNull(this)
            assertEquals("it", code)
            assertEquals("it", tag)
            assertEquals("italiano", localizedName)
            assertNull(countryCode)
            assertNull(localizedCountryName)
        }

        with(retriever.fromTag("en-US")) {
            assertNotNull(this)
            assertEquals("en", code)
            assertEquals("en-US", tag)
            assertEquals("English (United States)", localizedName)
            assertEquals("US", countryCode)
            assertEquals("United States", localizedCountryName)
        }

        with(retriever.fromTag("En-us")) {
            assertNotNull(this)
            assertEquals("en", code)
            assertEquals("en-US", tag)
            assertEquals("English (United States)", localizedName)
            assertEquals("US", countryCode)
            assertEquals("United States", localizedCountryName)
        }

        with(retriever.fromTag("fr-CA")) {
            assertNotNull(this)
            assertEquals("fr", code)
            assertEquals("fr-CA", tag)
            assertEquals("français (Canada)", localizedName)
            assertEquals("CA", countryCode)
            assertEquals("Canada", localizedCountryName)
        }

        assertNull(retriever.fromTag("nonexistent"))

        assertTrue(retriever.all.iterator().hasNext())
    }
}
