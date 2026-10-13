package com.quarkdown.pm.repo

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertNull

/**
 * Tests for [PackageRepositories].
 */
class PackageRepositoriesTest {
    @Test
    fun `default registry maps the atoms scope to the atoms repository`() {
        assertIs<AtomsPackageRepository>(PackageRepositories.default().forScope(ATOMS_SCOPE))
    }

    @Test
    fun `unknown scope is null`() {
        assertNull(PackageRepositories.default().forScope("nope"))
    }
}
