package com.quarkdown.interaction.cdp

import com.quarkdown.interaction.executable.ChromiumWrapper
import com.quarkdown.interaction.os.OsUtils
import org.junit.jupiter.api.Assumptions.assumeTrue
import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for [ChromiumInteraction]'s browser lifecycle handling.
 */
class ChromiumInteractionTest {
    private val directory: File =
        createTempDirectory()
            .toFile()

    /**
     * @return a fake browser executable that produces no output and never exits on its own
     */
    private fun silentFakeBrowser(): ChromiumWrapper {
        val script = File(directory, "silent-browser.sh")
        script.writeText("#!/bin/sh\nsleep 60\n")
        script.setExecutable(true)
        return ChromiumWrapper(path = script.absolutePath)
    }

    @Test
    fun `startup times out on a browser that never announces its endpoint`() {
        assumeTrue(OsUtils.dependent(windows = { false }, unix = { true }))

        val interaction =
            ChromiumInteraction(
                browser = silentFakeBrowser(),
                startupTimeoutMs = 500,
            )
        val exception =
            assertFailsWith<IllegalStateException> {
                interaction.withPage {}
            }
        assertTrue("did not announce" in exception.message!!)
    }

    @Test
    fun `startup times out on a browser that stalls on a partial output line`() {
        assumeTrue(OsUtils.dependent(windows = { false }, unix = { true }))

        // No trailing newline: the announcement wait must not block on the incomplete line.
        val script = File(directory, "stalling-browser.sh")
        script.writeText("#!/bin/sh\nprintf 'DevTools list'\nsleep 60\n")
        script.setExecutable(true)

        val interaction =
            ChromiumInteraction(
                browser = ChromiumWrapper(path = script.absolutePath),
                startupTimeoutMs = 500,
            )
        val exception =
            assertFailsWith<IllegalStateException> {
                interaction.withPage {}
            }
        assertTrue("did not announce" in exception.message!!)
    }

    @Test
    fun `startup fails on a browser that exits without announcing its endpoint`() {
        assumeTrue(OsUtils.dependent(windows = { false }, unix = { true }))

        val script = File(directory, "exiting-browser.sh")
        script.writeText("#!/bin/sh\nexit 0\n")
        script.setExecutable(true)

        val exception =
            assertFailsWith<IllegalStateException> {
                ChromiumInteraction(browser = ChromiumWrapper(path = script.absolutePath)).withPage {}
            }
        assertTrue("exited" in exception.message!!)
    }
}
