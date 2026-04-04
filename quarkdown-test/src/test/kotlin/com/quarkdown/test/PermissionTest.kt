package com.quarkdown.test

import com.quarkdown.core.permissions.MissingPermissionException
import com.quarkdown.core.permissions.Permission.GlobalRead
import com.quarkdown.core.permissions.Permission.NativeContent
import com.quarkdown.core.permissions.Permission.ProjectRead
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.test.util.DATA_FOLDER
import com.quarkdown.test.util.execute
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

/**
 * Integration tests for the permission system.
 * Verifies that permission checks are enforced end-to-end through the compilation pipeline.
 */
class PermissionTest {
    // ProjectRead / GlobalRead

    @Test
    fun `read with ProjectRead succeeds for in-project file`() {
        execute(
            ".read {code.txt}",
            permissions = setOf(ProjectRead),
        ) {
            // code.txt is under the default working directory: no exception expected.
        }
    }

    @Test
    fun `read without read permission fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                ".read {code.txt}",
                permissions = emptySet(),
            ) {}
        }
    }

    @Test
    fun `read file outside project with only ProjectRead fails`() {
        // Use a subdirectory as the working directory so that ../code.txt falls outside it.
        assertFailsWith<MissingPermissionException> {
            execute(
                ".read {../code.txt}",
                workingDirectory = File(DATA_FOLDER, "csv"),
                permissions = setOf(ProjectRead),
            ) {}
        }
    }

    @Test
    fun `read file outside project with GlobalRead succeeds`() {
        execute(
            ".read {../code.txt}",
            workingDirectory = File(DATA_FOLDER, "csv"),
            permissions = setOf(ProjectRead, GlobalRead),
        ) {}
    }

    @Test
    fun `csv with ProjectRead succeeds`() {
        execute(
            ".csv {csv/people.csv}",
            permissions = setOf(ProjectRead),
        ) {}
    }

    @Test
    fun `csv without read permission fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                ".csv {csv/people.csv}",
                permissions = emptySet(),
            ) {}
        }
    }

    @Test
    fun `bibliography with ProjectRead succeeds`() {
        execute(
            ".bibliography {bib/bibliography.bib} breakpage:{no}",
            permissions = setOf(ProjectRead),
        ) {}
    }

    @Test
    fun `bibliography without read permission fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                ".bibliography {bib/bibliography.bib} breakpage:{no}",
                permissions = emptySet(),
            ) {}
        }
    }

    @Test
    fun `font with ProjectRead succeeds`() {
        execute(
            ".font main:{font/NotoSans-Regular.ttf}",
            permissions = setOf(ProjectRead),
        ) {}
    }

    @Test
    fun `font without read permission fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                ".font main:{font/NotoSans-Regular.ttf}",
                permissions = emptySet(),
            ) {}
        }
    }

    // NativeContent

    @Test
    fun `html with NativeContent succeeds`() {
        execute(
            ".html {<b>hello</b>}",
            permissions = setOf(NativeContent),
        ) {}
    }

    @Test
    fun `html without NativeContent fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                ".html {<b>hello</b>}",
                permissions = emptySet(),
            ) {}
        }
    }

    // Subdocument read permissions

    @Test
    fun `subdocument with ProjectRead succeeds`() {
        execute(
            "[Link](subdoc/simple-1.qd)",
            permissions = setOf(ProjectRead),
        ) {}
    }

    @Test
    fun `subdocument without read permission fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                "[Link](subdoc/simple-1.qd)",
                permissions = emptySet(),
            ) {}
        }
    }

    @Test
    fun `subdocument outside project with only ProjectRead fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                "[Link](../subdoc/simple-1.qd)",
                workingDirectory = File(DATA_FOLDER, "csv"),
                permissions = setOf(ProjectRead),
            ) {}
        }
    }

    @Test
    fun `subdocument outside project with GlobalRead succeeds`() {
        execute(
            "[Link](../subdoc/simple-1.qd)",
            workingDirectory = File(DATA_FOLDER, "csv"),
            permissions = setOf(GlobalRead),
        ) {}
    }

    @Test
    fun `subdocument function without read permission fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                ".subdocument {subdoc/simple-1.qd}",
                permissions = emptySet(),
            ) {}
        }
    }

    @Test
    fun `failing subdocument access renders error with non-strict handler`() {
        execute(
            "[Link](subdoc/simple-1.qd)",
            permissions = emptySet(),
            errorHandler = BasePipelineErrorHandler(),
        ) {
            assertContains(it, "Error")
            assertContains(it, "Cannot access")
        }
    }
}
