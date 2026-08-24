package com.quarkdown.server.browser

import com.quarkdown.interaction.os.OsFamily
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [KnownLocationBrowserLauncher].
 *
 * Platform, environment and file existence are all injected, so every platform's resolution
 * is exercised regardless of the host running the suite.
 */
class KnownLocationBrowserLauncherTest {
    private fun launcher(
        browser: String,
        family: OsFamily,
        env: Map<String, String> = emptyMap(),
        existing: Set<String> = emptySet(),
    ) = KnownLocationBrowserLauncher(
        browser = browser,
        family = family,
        env = env::get,
        isExecutable = existing::contains,
    )

    @Test
    fun `resolves chrome from the macOS applications directory`() {
        val path = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
        val launcher = launcher("chrome", OsFamily.MACOS, existing = setOf(path))
        assertTrue(launcher.isValid)
        assertEquals(path, launcher.executable)
    }

    @Test
    fun `resolves chrome from Windows Program Files`() {
        val programFiles = "C:\\Program Files"
        val path = "$programFiles\\Google\\Chrome\\Application\\chrome.exe"
        val launcher =
            launcher(
                "chrome",
                OsFamily.WINDOWS,
                env = mapOf("ProgramFiles" to programFiles),
                existing = setOf(path),
            )
        assertEquals(path, launcher.executable)
    }

    @Test
    fun `falls back to the 32-bit Program Files on Windows`() {
        val programFilesX86 = "C:\\Program Files (x86)"
        val path = "$programFilesX86\\Mozilla Firefox\\firefox.exe"
        val launcher =
            launcher(
                "firefox",
                OsFamily.WINDOWS,
                env = mapOf("ProgramFiles" to "C:\\Program Files", "ProgramFiles(x86)" to programFilesX86),
                existing = setOf(path),
            )
        assertEquals(path, launcher.executable)
    }

    @Test
    fun `resolves chromium from PATH on Linux`() {
        val launcher =
            launcher(
                "chromium",
                OsFamily.LINUX,
                env = mapOf("PATH" to "/usr/local/bin:/usr/bin"),
                existing = setOf("/usr/bin/chromium-browser"),
            )
        assertEquals("/usr/bin/chromium-browser", launcher.executable)
    }

    @Test
    fun `prefers the earlier PATH entry`() {
        val launcher =
            launcher(
                "firefox",
                OsFamily.LINUX,
                env = mapOf("PATH" to "/usr/local/bin:/usr/bin"),
                existing = setOf("/usr/local/bin/firefox", "/usr/bin/firefox"),
            )
        assertEquals("/usr/local/bin/firefox", launcher.executable)
    }

    @Test
    fun `is invalid when the browser is not installed`() {
        val launcher = launcher("chrome", OsFamily.MACOS, existing = emptySet())
        assertFalse(launcher.isValid)
        assertNull(launcher.executable)
    }

    @Test
    fun `is invalid for an unknown browser name`() {
        val launcher =
            launcher(
                "netscape",
                OsFamily.LINUX,
                env = mapOf("PATH" to "/usr/bin"),
                existing = setOf("/usr/bin/netscape"),
            )
        assertFalse(launcher.isValid)
    }

    @Test
    fun `browser name is case-insensitive`() {
        val path = "/Applications/Firefox.app/Contents/MacOS/firefox"
        assertEquals(path, launcher("FireFox", OsFamily.MACOS, existing = setOf(path)).executable)
    }

    @Test
    fun `windows candidates are skipped when Program Files is undefined`() {
        assertFalse(launcher("chrome", OsFamily.WINDOWS, env = emptyMap()).isValid)
    }

    @Test
    fun `every supported browser is known on every platform`() {
        OsFamily.entries.forEach { family ->
            KnownBrowserLocations.supportedBrowsers.forEach { browser ->
                val candidates = KnownBrowserLocations.candidatesOf(browser, family) { "C:\\Program Files" }
                assertTrue(
                    candidates.paths.isNotEmpty() || candidates.commands.isNotEmpty(),
                    "no candidates for $browser on $family",
                )
            }
        }
    }
}
