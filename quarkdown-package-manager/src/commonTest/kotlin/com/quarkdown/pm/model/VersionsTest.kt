package com.quarkdown.pm.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [latestVersion].
 */
class VersionsTest {
    @Test
    fun `picks the highest semantic version, not the highest string`() {
        assertEquals("1.10.0", latestVersion(listOf("1.9.0", "1.10.0", "1.2.3")))
    }

    @Test
    fun `a prerelease ranks below its release and above lower versions`() {
        assertEquals("2.0.0", latestVersion(listOf("2.0.0-beta.1", "2.0.0", "1.9.9")))
        assertEquals("2.0.0-beta.1", latestVersion(listOf("2.0.0-beta.1", "1.9.9")))
    }

    @Test
    fun `ignores names that are not semantic versions`() {
        assertEquals("1.0.0", latestVersion(listOf("latest", "1.0.0", "v2")))
        assertNull(latestVersion(listOf("latest", "drafts")))
    }

    @Test
    fun `nothing to choose from is null`() {
        assertNull(latestVersion(emptyList()))
    }
}
