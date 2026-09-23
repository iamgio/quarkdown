package com.quarkdown.core

import com.quarkdown.core.filesystem.DiskFileSystem
import com.quarkdown.core.filesystem.FsEntry
import com.quarkdown.core.permissions.MissingPermissionException
import com.quarkdown.core.permissions.Permission.GlobalRead
import com.quarkdown.core.permissions.Permission.NativeContent
import com.quarkdown.core.permissions.Permission.ProjectRead
import com.quarkdown.core.permissions.requirePermission
import com.quarkdown.core.permissions.requireReadPermission
import java.io.File
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Tests for the permission system primitives: [requirePermission] and [requireReadPermission].
 */
class PermissionTest {
    /** Creates a disk-backed [FsEntry] for the given absolute [path]. */
    private fun entry(path: String): FsEntry = DiskFileSystem().resolve(path)

    @Test
    fun `requirePermission succeeds when permission is granted`() {
        requirePermission(ProjectRead, granted = setOf(ProjectRead, NativeContent), message = "test")
    }

    @Test
    fun `requirePermission throws when permission is missing`() {
        assertFailsWith<MissingPermissionException> {
            requirePermission(GlobalRead, granted = setOf(ProjectRead), message = "test")
        }
    }

    @Test
    fun `requireReadPermission succeeds for file inside project with ProjectRead`() {
        val workingDir = File("/project")
        val holder =
            MockPermissionHolder(
                permissions = setOf(ProjectRead),
                rootFileSystem = DiskFileSystem(workingDir),
            )
        holder.requireReadPermission(entry("/project/src/file.txt"))
    }

    @Test
    fun `requireReadPermission throws for file outside project with only ProjectRead`() {
        val workingDir = File("/project")
        val holder =
            MockPermissionHolder(
                permissions = setOf(ProjectRead),
                rootFileSystem = DiskFileSystem(workingDir),
            )
        assertFailsWith<MissingPermissionException> {
            holder.requireReadPermission(entry("/other/file.txt"))
        }
    }

    @Test
    fun `requireReadPermission succeeds for file outside project with GlobalRead`() {
        val workingDir = File("/project")
        val holder =
            MockPermissionHolder(
                permissions = setOf(ProjectRead, GlobalRead),
                rootFileSystem = DiskFileSystem(workingDir),
            )
        holder.requireReadPermission(entry("/other/file.txt"))
    }

    @Test
    fun `requireReadPermission requires GlobalRead for symlink pointing outside project`() {
        val tempDir = Files.createTempDirectory("permissionSymlinkTest")
        try {
            val projectDir = tempDir.resolve("project").createDirectories()
            val outsideFile = Files.createFile(tempDir.resolve("secret.txt"))
            val symlink = Files.createSymbolicLink(projectDir.resolve("link.txt"), outsideFile)

            val holder =
                MockPermissionHolder(
                    permissions = setOf(ProjectRead),
                    rootFileSystem = DiskFileSystem(projectDir.toFile()),
                )
            assertFailsWith<MissingPermissionException> {
                holder.requireReadPermission(entry(symlink.toString()))
            }
        } finally {
            tempDir.toFile().deleteRecursively()
        }
    }

    @Test
    fun `requireReadPermission treats all files as global when rootFileSystem is null`() {
        val holderWithGlobal = MockPermissionHolder(permissions = setOf(GlobalRead), rootFileSystem = null)
        holderWithGlobal.requireReadPermission(entry("/any/file.txt"))

        val holderWithoutGlobal = MockPermissionHolder(permissions = setOf(ProjectRead), rootFileSystem = null)
        assertFailsWith<MissingPermissionException> {
            holderWithoutGlobal.requireReadPermission(entry("/any/file.txt"))
        }
    }
}
