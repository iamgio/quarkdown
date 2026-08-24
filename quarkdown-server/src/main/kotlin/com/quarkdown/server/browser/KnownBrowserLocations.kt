package com.quarkdown.server.browser

import com.quarkdown.interaction.os.OsFamily

/**
 * Places a browser executable may be found, in priority order.
 *
 * @param paths absolute paths to try, in order
 * @param commands executable names to look up on `PATH`, in order, if no [paths] match
 */
data class BrowserCandidates(
    val paths: List<String> = emptyList(),
    val commands: List<String> = emptyList(),
)

/**
 * Catalog of where each supported browser is installed on each platform.
 *
 * Quarkdown consults this itself rather than relying on a launcher script to export `BROWSER_<NAME>`
 * variables, so that browser preview works when the CLI runs as a native binary. An explicitly set
 * `BROWSER_<NAME>` still wins: this catalog is only consulted as a fallback.
 */
object KnownBrowserLocations {
    /** Browser names this catalog knows about. */
    val supportedBrowsers = setOf("chrome", "chromium", "firefox", "edge")

    private fun macOsApp(
        app: String,
        binary: String,
    ) = "/Applications/$app.app/Contents/MacOS/$binary"

    /**
     * Expands the Windows `Program Files` directories, both 64-bit and 32-bit,
     * dropping any that the environment does not define.
     */
    private fun programFiles(
        env: (String) -> String?,
        relative: String,
    ): List<String> =
        listOfNotNull(env("ProgramFiles"), env("ProgramFiles(x86)"))
            .map { "$it\\$relative" }

    /**
     * @param browser browser name, case-insensitive (e.g. `chrome`)
     * @param family operating system family to resolve locations for
     * @param env environment lookup, overridable for testing
     * @return where to look for [browser]'s executable, or empty candidates if unknown on this platform
     */
    fun candidatesOf(
        browser: String,
        family: OsFamily,
        env: (String) -> String? = System::getenv,
    ): BrowserCandidates =
        when (family) {
            OsFamily.MACOS ->
                when (browser.lowercase()) {
                    "chrome" -> BrowserCandidates(paths = listOf(macOsApp("Google Chrome", "Google Chrome")))
                    "chromium" -> BrowserCandidates(paths = listOf(macOsApp("Chromium", "Chromium")))
                    "firefox" -> BrowserCandidates(paths = listOf(macOsApp("Firefox", "firefox")))
                    "edge" -> BrowserCandidates(paths = listOf(macOsApp("Microsoft Edge", "Microsoft Edge")))
                    else -> BrowserCandidates()
                }

            OsFamily.WINDOWS ->
                when (browser.lowercase()) {
                    "chrome" -> BrowserCandidates(paths = programFiles(env, "Google\\Chrome\\Application\\chrome.exe"))
                    "chromium" -> BrowserCandidates(paths = programFiles(env, "Chromium\\Application\\chrome.exe"))
                    "firefox" -> BrowserCandidates(paths = programFiles(env, "Mozilla Firefox\\firefox.exe"))
                    "edge" -> BrowserCandidates(paths = programFiles(env, "Microsoft\\Edge\\Application\\msedge.exe"))
                    else -> BrowserCandidates()
                }

            // Linux and other Unix-like systems install browsers on the PATH rather than at fixed locations.
            OsFamily.LINUX, OsFamily.OTHER ->
                when (browser.lowercase()) {
                    "chrome" -> BrowserCandidates(commands = listOf("google-chrome", "google-chrome-stable"))
                    "chromium" -> BrowserCandidates(commands = listOf("chromium-browser", "chromium"))
                    "firefox" -> BrowserCandidates(commands = listOf("firefox"))
                    "edge" -> BrowserCandidates(commands = listOf("microsoft-edge", "microsoft-edge-stable"))
                    else -> BrowserCandidates()
                }
        }
}
