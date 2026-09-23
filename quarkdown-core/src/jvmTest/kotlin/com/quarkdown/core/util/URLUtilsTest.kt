package com.quarkdown.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for URL detection via [toUrlOrNull] and [isUrl],
 * whose discrimination between URLs and file paths drives media resolution and link resolution.
 */
class URLUtilsTest {
    @Test
    fun `absolute urls with a supported protocol are urls`() {
        assertNotNull("https://example.com/image.png".toUrlOrNull())
        assertNotNull("http://example.com".toUrlOrNull())
        assertNotNull("ftp://host/x".toUrlOrNull())
        assertNotNull("file:///tmp/x.png".toUrlOrNull())
        assertNotNull("mailto:x@y.com".toUrlOrNull())
    }

    @Test
    fun `protocol matching is case-insensitive`() {
        assertNotNull("HTTPS://EXAMPLE.COM/x".toUrlOrNull())
    }

    @Test
    fun `query and fragment are preserved`() {
        assertEquals(
            "https://example.com/a?q=1#frag",
            "https://example.com/a?q=1#frag".toUrlOrNull().toString(),
        )
    }

    @Test
    fun `a plain https url round-trips unchanged`() {
        assertEquals(
            "https://example.com/image.png",
            "https://example.com/image.png".toUrlOrNull().toString(),
        )
    }

    @Test
    fun `file paths are not urls`() {
        assertNull("media/photo.png".toUrlOrNull())
        assertNull("photo.png".toUrlOrNull())
        assertNull("/absolute/photo.png".toUrlOrNull())
    }

    @Test
    fun `windows drive paths are not urls`() {
        assertNull("C:/images/photo.png".toUrlOrNull())
        assertNull("C:\\images\\photo.png".toUrlOrNull())
    }

    @Test
    fun `unsupported protocols are not urls`() {
        assertNull("data:image/png;base64,AAA".toUrlOrNull())
        assertNull("customscheme://x".toUrlOrNull())
    }

    @Test
    fun `bare domains and empty strings are not urls`() {
        assertNull("example.com".toUrlOrNull())
        assertNull("".toUrlOrNull())
    }

    @Test
    fun `isUrl matches toUrlOrNull`() {
        assertTrue("https://example.com/image.png".isUrl)
        assertFalse("photo.png".isUrl)
    }
}
