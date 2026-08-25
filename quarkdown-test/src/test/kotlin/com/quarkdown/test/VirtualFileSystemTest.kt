package com.quarkdown.test

import com.quarkdown.core.filesystem.VirtualFileSystem
import com.quarkdown.core.permissions.MissingPermissionException
import com.quarkdown.core.permissions.Permission
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

/**
 * Tests for compiling documents whose inputs live entirely on a [VirtualFileSystem],
 * with no disk access.
 */
class VirtualFileSystemTest {
    private fun virtualFs(): VirtualFileSystem =
        VirtualFileSystem("/project").apply {
            write("data.txt", "Hello from memory")
            write("table.csv", "name,age\nAlice,30\nBob,40")
            write("lib/included.qd", ".var {fromlib} {included!}")
            write("sub/child.qd", "# Child")
        }

    @Test
    fun `reads a text file`() {
        execute(".read {data.txt}", fileSystem = virtualFs()) {
            assertContains(it, "Hello from memory")
        }
    }

    @Test
    fun `reads a csv table`() {
        execute(".csv {table.csv}", fileSystem = virtualFs()) {
            assertContains(it, "Alice")
            assertContains(it, "<table")
        }
    }

    @Test
    fun `includes a source file, branching the working directory`() {
        execute(
            ".include {lib/included.qd}\n\n.fromlib",
            fileSystem = virtualFs(),
        ) {
            assertContains(it, "included!")
        }
    }

    @Test
    fun `registers a subdocument`() {
        execute(
            "[Child](sub/child.qd)",
            fileSystem = virtualFs(),
        ) {
            // The hook runs for the root document (which links the child) and for the
            // child subdocument itself, whose content is compiled from the virtual tree.
            assertContains(it, "Child")
        }
    }

    @Test
    fun `lists files from the virtual tree`() {
        execute(".listfiles {lib} fullpath:{no}", fileSystem = virtualFs()) {
            assertContains(it, "included.qd")
        }
    }

    @Test
    fun `denies reads outside the virtual project without global permission`() {
        val fs = virtualFs().apply { write("/outside/secret.txt", "secret") }
        assertFailsWith<MissingPermissionException> {
            execute(
                ".read {/outside/secret.txt}",
                fileSystem = fs,
                permissions = setOf(Permission.ProjectRead),
            ) {}
        }
    }

    @Test
    fun `stores a virtual image in the media storage`() {
        val fs = virtualFs().apply { writeBytes("img/pixel.png", byteArrayOf(-119, 80, 78, 71)) }
        execute(
            "![Pixel](img/pixel.png)",
            fileSystem = fs,
            enableMediaStorage = true,
        ) {
            assertContains(it, "media/")
        }
    }
}
