package com.quarkdown.test

import com.quarkdown.core.permissions.MissingPermissionException
import com.quarkdown.core.permissions.Permission.ProcessAccess
import com.quarkdown.core.permissions.Permission.ProjectRead
import com.quarkdown.core.pipeline.error.BasePipelineErrorHandler
import com.quarkdown.test.util.execute
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith

/**
 * Integration tests for the `Process` stdlib module.
 * Verifies that environment variable access is properly gated by the [ProcessAccess] permission.
 */
class ProcessTest {
    // An environment variable that is always set on any platform
    private val safeEnvVar: String
        get() = if (System.getenv("HOME") != null) "HOME" else "USERPROFILE"

    @Test
    fun `env with ProcessAccess returns variable value`() {
        execute(
            ".env {$safeEnvVar}",
            permissions = setOf(ProcessAccess),
        ) {
            assert(it.isNotBlank())
        }
    }

    @Test
    fun `env with ProcessAccess for unset variable returns none`() {
        execute(
            ".env {QUARKDOWN_NONEXISTENT_VARIABLE_12345}",
            permissions = setOf(ProcessAccess),
        ) {
            assertContains(it, "None")
        }
    }

    @Test
    fun `env without ProcessAccess fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                ".env {PATH}",
                permissions = emptySet(),
            ) {}
        }
    }

    @Test
    fun `env with only ProjectRead fails`() {
        assertFailsWith<MissingPermissionException> {
            execute(
                ".env {PATH}",
                permissions = setOf(ProjectRead),
            ) {}
        }
    }

    @Test
    fun `env with otherwise falls back for unset variable`() {
        execute(
            ".env {QUARKDOWN_NONEXISTENT_VARIABLE_12345}::otherwise {fallback}",
            permissions = setOf(ProcessAccess),
        ) {
            assertContains(it, "fallback")
        }
    }

    @Test
    fun `env with otherwise uses value for set variable`() {
        execute(
            ".env {$safeEnvVar}::otherwise {QUARKDOWN_FALLBACK_SENTINEL}",
            permissions = setOf(ProcessAccess),
        ) {
            // The variable is always set, so the fallback should not appear.
            assert("QUARKDOWN_FALLBACK_SENTINEL" !in it)
        }
    }

    @Test
    fun `failing env access renders error with non-strict handler`() {
        execute(
            ".env {PATH}",
            permissions = emptySet(),
            errorHandler = BasePipelineErrorHandler(),
        ) {
            assertContains(it, "Error")
            assertContains(it, "Cannot read environment variable")
            assertContains(it, ">--allow process</")
        }
    }
}
