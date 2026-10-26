package com.quarkdown.core.util

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [sanitizeFileName].
 */
class SanitizeFileNameTest {
    private fun sanitize(input: String) = input.sanitizeFileName(replacement = "-")

    @Test
    fun `keeps letters, digits, dots and at signs`() {
        assertEquals("pic@1585920825.svg", sanitize("pic@1585920825.svg"))
        assertEquals("my-file_v2@1.jpg", sanitize("my-file_v2@1.jpg"))
    }

    @Test
    fun `replaces a space`() {
        assertEquals("my-file.jpg", sanitize("my file.jpg"))
    }

    @Test
    fun `keeps non-ASCII letters`() {
        assertEquals("café.png", sanitize("café.png"))
        assertEquals("日本語.txt", sanitize("日本語.txt"))
    }

    @Test
    fun `keeps combining marks`() {
        assertEquals("é.png", sanitize("é.png"))
    }

    @Test
    fun `replaces a run of disallowed characters with a single replacement`() {
        assertEquals("a-b-c.txt", sanitize("a  b?!c.txt"))
        assertEquals("a-b", sanitize("a/\\:*\"<>|b"))
    }

    @Test
    fun `replaces leading and trailing dots`() {
        assertEquals("-hidden-", sanitize(".hidden."))
        assertEquals("-", sanitize("."))
        assertEquals("--", sanitize(".."))
        assertEquals("-.a.-", sanitize("..a.."))
    }

    @Test
    fun `does not merge a boundary dot with an adjacent run`() {
        assertEquals("a--", sanitize("a ."))
        assertEquals("--a", sanitize(". a"))
    }

    @Test
    fun `keeps letters outside the basic multilingual plane`() {
        assertEquals("𝒜.txt", sanitize("𝒜.txt")) // 𝒜 (U+1D49C)
    }

    @Test
    fun `replaces an emoji with a single replacement`() {
        assertEquals("-.png", sanitize("😀.png")) // 😀 (U+1F600)
        assertEquals("a-b.png", sanitize("a😀 b.png"))
    }

    @Test
    fun `uses the given replacement`() {
        assertEquals("my_file.jpg", "my file.jpg".sanitizeFileName(replacement = "_"))
        assertEquals("myfile.jpg", "my file.jpg".sanitizeFileName(replacement = ""))
    }

    @Test
    fun `handles empty input`() {
        assertEquals("", sanitize(""))
    }
}
