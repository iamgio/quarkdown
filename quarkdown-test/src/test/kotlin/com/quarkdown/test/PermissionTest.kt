package com.quarkdown.test

import com.quarkdown.core.function.error.FunctionCallRuntimeException
import com.quarkdown.core.permissions.MissingPermissionException
import com.quarkdown.core.permissions.Permission.*
import com.quarkdown.test.util.DATA_FOLDER
import com.quarkdown.test.util.execute
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

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
        assertFailsWith<FunctionCallRuntimeException> {
            execute(
                ".read {code.txt}",
                permissions = emptySet(),
            ) {}
        }.also { assertIs<MissingPermissionException>(it.cause) }
    }

    @Test
    fun `read file outside project with only ProjectRead fails`() {
        // Use a subdirectory as the working directory so that ../code.txt falls outside it.
        assertFailsWith<FunctionCallRuntimeException> {
            execute(
                ".read {../code.txt}",
                workingDirectory = File(DATA_FOLDER, "csv"),
                permissions = setOf(ProjectRead),
            ) {}
        }.also { assertIs<MissingPermissionException>(it.cause) }
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
        assertFailsWith<FunctionCallRuntimeException> {
            execute(
                ".csv {csv/people.csv}",
                permissions = emptySet(),
            ) {}
        }.also { assertIs<MissingPermissionException>(it.cause) }
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
        assertFailsWith<FunctionCallRuntimeException> {
            execute(
                ".bibliography {bib/bibliography.bib} breakpage:{no}",
                permissions = emptySet(),
            ) {}
        }.also { assertIs<MissingPermissionException>(it.cause) }
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
        assertFailsWith<FunctionCallRuntimeException> {
            execute(
                ".font main:{font/NotoSans-Regular.ttf}",
                permissions = emptySet(),
            ) {}
        }.also { assertIs<MissingPermissionException>(it.cause) }
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
        assertFailsWith<FunctionCallRuntimeException> {
            execute(
                ".html {<b>hello</b>}",
                permissions = emptySet(),
            ) {}
        }.also { assertIs<MissingPermissionException>(it.cause) }
    }
}
