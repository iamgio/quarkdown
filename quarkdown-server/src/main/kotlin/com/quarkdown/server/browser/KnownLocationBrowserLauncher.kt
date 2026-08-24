package com.quarkdown.server.browser

import com.quarkdown.interaction.os.OsFamily
import com.quarkdown.interaction.os.OsUtils
import java.io.File

/**
 * Name of the environment variable listing the executable search paths.
 */
private const val PATH_ENV = "PATH"

/**
 * Browser launcher that finds a browser by name in the locations where the current platform
 * installs it, as listed by [KnownBrowserLocations].
 *
 * This is the fallback used when no `BROWSER_<NAME>` environment variable is set, and replaces
 * the browser detection a launcher script would otherwise have to perform.
 *
 * @param browser browser name, case-insensitive (e.g. `chrome`)
 * @param family operating system family to resolve locations for. Overridable for testing
 * @param env environment lookup. Overridable for testing
 * @param isExecutable predicate telling whether a path is a runnable file. Overridable for testing
 */
class KnownLocationBrowserLauncher(
    private val browser: String,
    private val family: OsFamily = OsUtils.family,
    private val env: (String) -> String? = System::getenv,
    private val isExecutable: (String) -> Boolean = { File(it).run { isFile && canExecute() } },
) : BrowserLauncher {
    /**
     * The executable this launcher resolved to, or `null` if the browser was not found.
     */
    val executable: String? by lazy {
        val candidates = KnownBrowserLocations.candidatesOf(browser, family, env)
        candidates.paths.firstOrNull(isExecutable)
            ?: candidates.commands.firstNotNullOfOrNull(::findOnPath)
    }

    /**
     * @param command executable name to look for
     * @return the absolute path of [command] as found on `PATH`, or `null` if absent
     */
    private fun findOnPath(command: String): String? =
        env(PATH_ENV)
            ?.split(File.pathSeparator)
            ?.asSequence()
            ?.filter { it.isNotBlank() }
            ?.map { File(it, command).path }
            ?.firstOrNull(isExecutable)

    override val isValid: Boolean
        get() = executable != null

    override fun launch(url: String) {
        ProcessBuilder(executable!!, url).start()
    }
}
