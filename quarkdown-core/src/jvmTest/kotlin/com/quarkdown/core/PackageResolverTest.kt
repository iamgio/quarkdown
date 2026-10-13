package com.quarkdown.core

import com.quarkdown.core.context.MutableContext
import com.quarkdown.core.context.packageResolver
import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.flavor.quarkdown.QuarkdownFlavor
import com.quarkdown.core.packages.PackageReferenceSyntax
import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.core.packages.PackageResolver
import com.quarkdown.core.packages.UnsupportedPackageResolutionException
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.pipeline.PipelineOptions
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests for the package resolution contract exposed by core.
 */
class PackageResolverTest {
    private val projectRoot = VirtualFileSystem("/project").resolve("/project")

    @Test
    fun `reference syntax is detected by prefix only`() {
        assertTrue(PackageReferenceSyntax.isReference("@atoms/cv:1.0.0"))
        assertTrue(PackageReferenceSyntax.isReference("@broken"))
        assertFalse(PackageReferenceSyntax.isReference("chapters/intro.qd"))
        assertFalse(PackageReferenceSyntax.isReference(""))
    }

    @Test
    fun `unsupported resolver rejects both operations`() {
        assertFailsWith<UnsupportedPackageResolutionException> { PackageResolver.Unsupported.locate("@atoms/cv:1.0.0", projectRoot) }
        assertFailsWith<UnsupportedPackageResolutionException> { PackageResolver.Unsupported.install("@atoms/cv:1.0.0", projectRoot) }
    }

    @Test
    fun `resolution exception names the reference`() {
        val exception = PackageResolutionException("@atoms/cv:1.0.0", "not found")
        assertContains(exception.message.orEmpty(), "@atoms/cv:1.0.0")
        assertContains(exception.message.orEmpty(), "not found")
    }

    @Test
    fun `package install permission is granted by default`() {
        assertTrue(Permission.PackageInstall in Permission.DEFAULT_SET)
        assertTrue(Permission.PackageInstall in Permission.ALL)
        assertEquals("package-install", Permission.PackageInstall.name)
    }

    @Test
    fun `pipeline options default to the unsupported resolver`() {
        assertSame(PackageResolver.Unsupported, PipelineOptions().packageResolver)
    }

    @Test
    fun `context without a pipeline falls back to the unsupported resolver`() {
        assertSame(PackageResolver.Unsupported, MutableContext(QuarkdownFlavor).packageResolver)
    }
}
