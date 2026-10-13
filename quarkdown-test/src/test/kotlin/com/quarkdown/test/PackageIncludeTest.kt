package com.quarkdown.test

import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.function.error.UnresolvedReferenceException
import com.quarkdown.core.packages.PackageResolutionException
import com.quarkdown.core.packages.UnsupportedPackageResolutionException
import com.quarkdown.core.permissions.MissingPermissionException
import com.quarkdown.core.permissions.Permission
import com.quarkdown.test.util.FakePackageResolver
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for `.include` of package references (`@scope/name:version`).
 */
class PackageIncludeTest {
    private val cv = "@atoms/cv:1.0.0"

    private fun projectFs() = VirtualFileSystem("/project").apply { write("main.qd", "") }

    private fun installedFs() =
        projectFs().apply {
            write("@atoms/cv/main.qd", "# From CV\n\n.var {cvname} {Installed}")
        }

    private val cvFiles =
        mapOf(
            "main.qd" to "# Downloaded CV\n\n.include {parts/intro.qd}",
            "parts/intro.qd" to "Intro from package",
        )

    @Test
    fun `installed package is included without installing`() {
        val fs = installedFs()
        val resolver = FakePackageResolver(fs)
        execute(".include {$cv}\n\n.cvname", fileSystem = fs, packageResolver = resolver) {
            assertContains(it, "From CV")
            assertContains(it, "Installed")
        }
        assertEquals(listOf(cv), resolver.located)
        assertEquals(emptyList(), resolver.installed)
    }

    @Test
    fun `missing package is installed once and its relative includes resolve from the package root`() {
        val fs = projectFs()
        val resolver = FakePackageResolver(fs, mapOf(cv to cvFiles))
        execute(".include {$cv}", fileSystem = fs, packageResolver = resolver) {
            assertContains(it, "Downloaded CV")
            assertContains(it, "Intro from package")
        }
        assertEquals(listOf(cv), resolver.installed)
    }

    @Test
    fun `install is denied without the package-install permission`() {
        val fs = projectFs()
        val resolver = FakePackageResolver(fs, mapOf(cv to cvFiles))
        assertFailsWith<MissingPermissionException> {
            execute(
                ".include {$cv}",
                fileSystem = fs,
                packageResolver = resolver,
                permissions = Permission.DEFAULT_SET - Permission.PackageInstall,
            ) {}
        }
        assertEquals(emptyList(), resolver.installed)
    }

    @Test
    fun `unknown package fails with the reference in the message`() {
        val fs = projectFs()
        val exception =
            assertFailsWith<PackageResolutionException> {
                execute(".include {@atoms/nope:1.0.0}", fileSystem = fs, packageResolver = FakePackageResolver(fs)) {}
            }
        assertContains(exception.message.orEmpty(), "@atoms/nope:1.0.0")
    }

    @Test
    fun `package references are rejected when the host provides no resolver`() {
        assertFailsWith<UnsupportedPackageResolutionException> {
            execute(".include {$cv}", fileSystem = projectFs()) {}
        }
    }

    @Test
    fun `includeall mixes files and package references`() {
        val fs = installedFs().apply { write("local.qd", "Local file") }
        execute(
            ".includeall\n    - local.qd\n    - $cv",
            fileSystem = fs,
            packageResolver = FakePackageResolver(fs),
        ) {
            assertContains(it, "Local file")
            assertContains(it, "From CV")
        }
    }

    @Test
    fun `scope and subdocument sandboxes include the package content`() {
        listOf("scope", "subdocument").forEach { sandbox ->
            val fs = installedFs()
            execute(".include {$cv} sandbox:{$sandbox}", fileSystem = fs, packageResolver = FakePackageResolver(fs)) {
                assertContains(it, "From CV", message = sandbox)
            }
        }
    }

    @Test
    fun `scope and subdocument sandboxes keep package declarations out of the main context`() {
        listOf("scope", "subdocument").forEach { sandbox ->
            val fs = installedFs()
            assertFailsWith<UnresolvedReferenceException>(sandbox) {
                execute(
                    ".include {$cv} sandbox:{$sandbox}\n\n.cvname",
                    fileSystem = fs,
                    packageResolver = FakePackageResolver(fs),
                ) {}
            }
        }
    }
}
