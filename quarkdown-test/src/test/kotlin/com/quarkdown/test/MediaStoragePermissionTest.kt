package com.quarkdown.test

import com.quarkdown.core.permissions.MissingPermissionException
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.permissions.Permission.*
import com.quarkdown.test.util.DATA_FOLDER
import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Integration tests for permission enforcement in the media storage system.
 * Verifies that local and remote media access is properly gated by the granted permissions.
 */
class MediaStoragePermissionTest {
    @Test
    fun `local media with ProjectRead succeeds`() {
        execute(
            "![Quarkdown](img/icon.png)",
            enableMediaStorage = true,
            permissions = setOf(ProjectRead),
        ) {}
    }

    @Test
    fun `local media without read permission fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                "![Quarkdown](img/icon.png)",
                enableMediaStorage = true,
                permissions = setOf(NativeContent),
            ) {}
        }
    }

    @Test
    fun `local media outside project with only ProjectRead fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                "![Quarkdown](../img/icon.png)",
                workingDirectory = File(DATA_FOLDER, "csv"),
                enableMediaStorage = true,
                permissions = setOf(ProjectRead),
            ) {}
        }
    }

    @Test
    fun `local media outside project with GlobalRead succeeds`() {
        execute(
            "![Quarkdown](../img/icon.png)",
            workingDirectory = File(DATA_FOLDER, "csv"),
            enableMediaStorage = true,
            permissions = setOf(ProjectRead, GlobalRead),
        ) {}
    }

    @Test
    fun `remote media with NetworkAccess succeeds`() {
        execute(
            "![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)",
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = true),
            enableMediaStorage = true,
            permissions = Permission.DEFAULT_SET + NetworkAccess,
        ) {}
    }

    @Test
    fun `remote media without NetworkAccess fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                "![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)",
                options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = true),
                enableMediaStorage = true,
                permissions = setOf(ProjectRead),
            ) {}
        }
    }

    @Test
    fun `mixed local and remote media with full permissions succeeds`() {
        execute(
            """
            ![Quarkdown](img/icon.png)
            ![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)
            """.trimIndent(),
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = true),
            enableMediaStorage = true,
            permissions = setOf(ProjectRead, NetworkAccess),
        ) {}
    }

    @Test
    fun `mixed local and remote media without NetworkAccess fails on remote`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                """
                ![Quarkdown](img/icon.png)
                ![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)
                """.trimIndent(),
                options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = true),
                enableMediaStorage = true,
                permissions = setOf(ProjectRead),
            ) {}
        }
    }

    @Test
    fun `remote media not stored when storage is disabled does not require NetworkAccess`() {
        execute(
            "![Banner](https://raw.githubusercontent.com/iamgio/quarkdown/project-files/images/tbanner-light.svg)",
            options = DEFAULT_OPTIONS.copy(enableRemoteMediaStorage = false),
            enableMediaStorage = true,
            permissions = setOf(ProjectRead),
        ) {}
    }

    @Test
    fun `no permissions required when media storage is disabled`() {
        execute(
            "![Quarkdown](img/icon.png)",
            enableMediaStorage = false,
            permissions = emptySet(),
        ) {}
    }
}
