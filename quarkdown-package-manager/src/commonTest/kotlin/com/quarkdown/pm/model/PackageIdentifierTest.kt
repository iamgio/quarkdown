package com.quarkdown.pm.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for [PackageIdentifier] validation.
 */
class PackageIdentifierTest {
    @Test
    fun `accepts single path segments`() {
        val identifier = PackageIdentifier(scope = "atoms", name = "my-cv_2.0")
        assertEquals("atoms", identifier.scope)
        assertEquals("my-cv_2.0", identifier.name)
    }

    @Test
    fun `rejects scope or name that is not a single path segment`() {
        listOf("", " ", "a/b", "a\\b", "a:b", "a b", ".", "..").forEach { invalid ->
            assertFailsWith<IllegalArgumentException>("scope '$invalid'") { PackageIdentifier(scope = invalid, name = "cv") }
            assertFailsWith<IllegalArgumentException>("name '$invalid'") { PackageIdentifier(scope = "atoms", name = invalid) }
        }
    }

    @Test
    fun `reference rejects version that is not a single path segment`() {
        listOf("", "1.0/0", "..", ".", "1 0", "a:b").forEach { invalid ->
            assertFailsWith<IllegalArgumentException>(invalid) {
                PackageReference(PackageIdentifier("atoms", "cv"), invalid)
            }
        }
    }
}
