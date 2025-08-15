package com.quarkdown.server.browser

/**
 * Prefix used to look up browser environment variables.
 * Example: `BROWSER_CHROME`, `BROWSER_FIREFOX`, etc.
 */
private const val ENV_PREFIX = "BROWSER_"

/**
 * Launcher of browsers whose path is stored in environment variables (`BROWSER_<env>`).
 *
 * @property env the environment variable suffix (e.g., "chrome" for BROWSER_CHROME).
 * @property envValue the resolved environment variable value if set.
 */
abstract class EnvBrowserLauncher(
    private val env: String,
) : BrowserLauncher {
    /**
     * The value of the environment variable for the browser, if set.
     */
    protected val envValue: String?
        get() = System.getenv(ENV_PREFIX + env.uppercase())
}

/**
 * Simple implementation of [EnvBrowserLauncher] that launches a browser if the environment variable is set,
 * passing the URL as the first argument to the browser executable.
 */
open class SimpleEnvBrowserLauncher(
    env: String,
) : EnvBrowserLauncher(env) {
    override val isValid: Boolean
        get() = super.envValue != null && super.envValue!!.isNotBlank()

    override fun launch(url: String) {
        val browserPath = super.envValue!!
        val command = arrayOf(browserPath, url)
        Runtime.getRuntime().exec(command)
    }
}
