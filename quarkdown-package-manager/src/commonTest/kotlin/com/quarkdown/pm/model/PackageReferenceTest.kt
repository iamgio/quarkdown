package com.quarkdown.pm.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [PackageReference] parsing.
 */
class PackageReferenceTest {
    @Test
    fun `parses scope name and version`() {
        val reference = PackageReference.parse("@atoms/cv:1.0.0")
        assertEquals(PackageIdentifier(scope = "atoms", name = "cv"), reference.identifier)
        assertEquals("1.0.0", reference.version)
    }

    @Test
    fun `version may contain any text but slashes`() {
        assertEquals("2.0.0-beta+build.7", PackageReference.parse("@atoms/cv:2.0.0-beta+build.7").version)
    }

    @Test
    fun `rejects malformed references`() {
        listOf(
            "atoms/cv:1.0.0",
            "@atoms:1.0.0",
            "@atoms/cv",
            "@/cv:1.0.0",
            "@atoms/:1.0.0",
            "@atoms/cv:",
            "@atoms/cv/extra:1.0.0",
            "@atoms/cv:1.0/0",
            "@atoms/..:1.0.0",
            "@../cv:1.0.0",
            "@atoms/.:1.0.0",
            "@atoms/ :1.0.0",
            "@atoms/my cv:1.0.0",
            "@atoms/cv:..",
            "@atoms/cv:1.0 beta",
            "@atoms\\x/cv:1.0.0",
            "@atoms/cv:1.0\\0",
            "@",
            "",
        ).forEach { malformed ->
            assertFailsWith<IllegalArgumentException>(malformed) { PackageReference.parse(malformed) }
        }
    }

    @Test
    fun `recognizes the reference prefix`() {
        assertTrue(PackageReference.isReference("@atoms/cv:1.0.0"))
        assertTrue(PackageReference.isReference("@broken"))
        assertFalse(PackageReference.isReference("chapters/intro.qd"))
        assertFalse(PackageReference.isReference(""))
    }

    @Test
    fun `round-trips through toString`() {
        val text = "@atoms/cv:1.0.0"
        assertEquals(text, PackageReference.parse(text).toString())
    }
}
