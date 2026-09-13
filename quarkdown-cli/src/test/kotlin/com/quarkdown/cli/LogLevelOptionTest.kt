package com.quarkdown.cli

import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.testing.test
import com.quarkdown.cli.exec.CompileCommand
import com.quarkdown.core.log.Log
import com.quarkdown.core.log.LogLevel
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Tests for the `--log-level` option and its `QD_LOG_LEVEL` environment variable fallback.
 */
class LogLevelOptionTest : TempDirectory() {
    private val main = File(directory, "main.qd")

    @BeforeTest
    fun setup() {
        Log.level = LogLevel.INFO
        main.writeText("Hello, world!")
    }

    @AfterTest
    fun tearDown() {
        Log.level = LogLevel.INFO
    }

    @Test
    fun `root option sets the log level`() {
        QuarkdownCommand().test("--log-level=warn")
        assertEquals(LogLevel.WARN, Log.level)
    }

    @Test
    fun `environment variable sets the log level`() {
        QuarkdownCommand().test("", envvars = mapOf(LOG_LEVEL_ENV to "error"))
        assertEquals(LogLevel.ERROR, Log.level)
    }

    @Test
    fun `option takes precedence over the environment variable`() {
        QuarkdownCommand().test("--log-level=debug", envvars = mapOf(LOG_LEVEL_ENV to "error"))
        assertEquals(LogLevel.DEBUG, Log.level)
    }

    @Test
    fun `matching is case-insensitive`() {
        QuarkdownCommand().test("--log-level=NONE")
        assertEquals(LogLevel.NONE, Log.level)
    }

    @Test
    fun `invalid value is rejected`() {
        val result = QuarkdownCommand().test("--log-level=verbose")
        assertNotEquals(0, result.statusCode)
        assertEquals(LogLevel.INFO, Log.level)
    }

    @Test
    fun `option applies before running a subcommand`() {
        val out = File(directory, "out")
        QuarkdownCommand()
            .subcommands(CompileCommand())
            .test(listOf("--log-level", "warn", "compile", main.absolutePath, "-o", out.absolutePath))
        assertEquals(LogLevel.WARN, Log.level)
    }
}
