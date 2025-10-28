package com.quarkdown.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.testing.test
import com.quarkdown.cli.server.browserLauncherOption
import com.quarkdown.server.browser.BrowserLauncher
import com.quarkdown.server.browser.DefaultBrowserLauncher
import com.quarkdown.server.browser.EnvBrowserLauncher
import com.quarkdown.server.browser.NoneBrowserLauncher
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertIs

/**
 * Mock command to test browser launcher selection.
 * Environment variables are simulated via [env].
 */
private class MockCommand(
    env: Map<String, String>,
) : CliktCommand() {
    val browserLauncher by browserLauncherOption(envLookup = env::get)

    override fun run() {}
}

/**
 * Tests for browser launcher selection via [browserLauncherOption].
 */
class BrowserLauncherSelectionTest {
    private fun test(
        value: String? = null,
        env: Map<String, String> = emptyMap(),
    ): BrowserLauncher? =
        MockCommand(env)
            .also {
                val argv =
                    if (value != null) {
                        arrayOf("--browser", value)
                    } else {
                        emptyArray()
                    }
                it.test(argv)
            }.browserLauncher

    @Test
    fun fallback() {
        assertIs<NoneBrowserLauncher>(test())
    }

    @Test
    fun `default choice`() {
        assertIs<DefaultBrowserLauncher>(test("default"))
    }

    @Test
    fun `none choice`() {
        assertIs<NoneBrowserLauncher>(test("none"))
    }

    @Test
    fun `from env`() {
        val choice = "chrome"
        val envName = "BROWSER_${choice.uppercase()}"
        assertIs<EnvBrowserLauncher>(test(choice, env = mapOf(envName to "/path/to/chrome")))
    }

    @Test
    fun `invalid from env`() {
        val choice = "nonexistentbrowser"
        assertFails { test(choice) }
    }

    @Test
    fun `invalid from path`() {
        val path = "path/to/nonexistent/browser"
        assertFails { test(path) }
    }
}
