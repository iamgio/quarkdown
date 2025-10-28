package com.quarkdown.server.browser

/**
 * Prefix used to look up browser environment variables.
 * Example: `BROWSER_CHROME`, `BROWSER_FIREFOX`, etc.
 */
private const val ENV_PREFIX = "BROWSER_"

/**
 * Launcher of browsers whose path is stored in environment variables (`BROWSER_<env>`)
 * passing the URL as the first argument to the browser executable.
 *
 * @param env the environment variable suffix (e.g., `chrome` for `BROWSER_CHROME`).
 * @param envLookup function to look up environment variable values.
 *                  If different from `System::getenv`, it can be used for testing purposes
 */
class EnvBrowserLauncher(
    private val env: String,
    private val envLookup: (String) -> String?,
) : BrowserLauncher {
    /**
     * The name of the environment variable for the browser.
     */
    val envName: String
        get() = ENV_PREFIX + env.uppercase()

    /**
     * The value of the environment variable for the browser, if set.
     */
    private val envValue: String? by lazy {
        this.envLookup(envName)
    }

    override val isValid: Boolean
        get() = this.envValue.isNullOrBlank().not()

    override fun launch(url: String) {
        val browserPath = this.envValue!!
        val command = arrayOf(browserPath, url)
        Runtime.getRuntime().exec(command)
    }
}
