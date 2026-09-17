package com.quarkdown.installlayout

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for [InstallDirectoryResolver]'s explicit-home strategy, which is what lets a packager,
 * or a build with no inspectable code location such as a native image, name its installation.
 */
class InstallDirectoryResolverTest {
    private companion object {
        const val HOME_PROPERTY = "quarkdown.home"
    }

    @AfterTest
    fun tearDown() {
        System.clearProperty(HOME_PROPERTY)
    }

    /**
     * @param markerName subdirectory identifying the directory as an install layout
     * @return a temporary directory containing a `lib/<markerName>` subtree
     */
    private fun installRoot(markerName: String = "html"): File =
        createTempDirectory("quarkdown-install").toFile().also {
            it.resolve("lib").resolve(markerName).mkdirs()
        }

    @Test
    fun `the install root resolves to its lib directory`() {
        val root = installRoot()
        System.setProperty(HOME_PROPERTY, root.absolutePath)

        val layout = InstallDirectoryResolver.resolve()

        assertEquals(root.resolve("lib").canonicalFile, layout.file.canonicalFile)
    }

    @Test
    fun `the lib directory itself is accepted`() {
        val lib = installRoot().resolve("lib")
        System.setProperty(HOME_PROPERTY, lib.absolutePath)

        val layout = InstallDirectoryResolver.resolve()

        assertEquals(lib.canonicalFile, layout.file.canonicalFile)
    }

    @Test
    fun `a directory holding only quarkdown libraries is accepted`() {
        val root = installRoot(markerName = "qd")
        System.setProperty(HOME_PROPERTY, root.absolutePath)

        assertEquals(
            root.resolve("lib").canonicalFile,
            InstallDirectoryResolver.resolve().file.canonicalFile,
        )
    }

    @Test
    fun `a directory that is not an installation is reported rather than ignored`() {
        val empty = createTempDirectory("quarkdown-not-an-install").toFile()
        System.setProperty(HOME_PROPERTY, empty.absolutePath)

        val exception = assertFailsWith<IllegalStateException> { InstallDirectoryResolver.resolve() }
        assertTrue(HOME_PROPERTY in exception.message!!)
        assertTrue(empty.absolutePath in exception.message!!)
    }
}
