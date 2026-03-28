package com.quarkdown.test

import com.quarkdown.core.permissions.MissingPermissionException
import com.quarkdown.core.permissions.Permission
import com.quarkdown.core.permissions.Permission.GlobalRead
import com.quarkdown.core.permissions.Permission.NativeContent
import com.quarkdown.core.permissions.Permission.NetworkAccess
import com.quarkdown.core.permissions.Permission.ProjectRead
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.test.util.DATA_FOLDER
import com.quarkdown.test.util.DEFAULT_OPTIONS
import com.quarkdown.test.util.execute
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
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
    fun `local media as reference image without read permission fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                """
                [icon]: img/icon.png
                ![Quarkdown][icon]
                """.trimIndent(),
                enableMediaStorage = true,
                permissions = setOf(NativeContent),
            ) {}
        }
    }

    @Test
    fun `failing media access renders error with non-strict handler`() {
        execute(
            "![Quarkdown](img/icon.png)",
            enableMediaStorage = true,
            permissions = setOf(),
            errorHandler = BasePipelineErrorHandler(),
        ) {
            assertContains(it, "Error")
            assertContains(it, "Cannot access")
            assertContains(it, "img${File.separator}icon.png")
        }
    }

    @Test
    fun `failing media access as reference image renders error with non-strict handler`() {
        execute(
            """
            [icon]: img/icon.png
            ![Quarkdown][icon]
            """.trimIndent(),
            enableMediaStorage = true,
            permissions = setOf(),
            errorHandler = BasePipelineErrorHandler(),
        ) {
            assertContains(it, "Error")
            assertContains(it, "Cannot access")
            assertContains(it, "img${File.separator}icon.png")
        }
    }

    @Test
    fun `unresolved media ignores missing permissions`() {
        execute(
            "![Quarkdown](img/nonexistent.png)",
            enableMediaStorage = true,
            permissions = setOf(),
        ) {
            assertContains(it, "src=\"img/nonexistent.png\"")
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
