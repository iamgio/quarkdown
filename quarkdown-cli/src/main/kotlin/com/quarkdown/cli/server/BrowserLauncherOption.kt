package com.quarkdown.cli.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.OptionDelegate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.quarkdown.core.log.Log
import com.quarkdown.server.browser.BrowserLauncher
import com.quarkdown.server.browser.DefaultBrowserLauncher
import com.quarkdown.server.browser.EnvBrowserLauncher
import com.quarkdown.server.browser.NoneBrowserLauncher
import com.quarkdown.server.browser.PathBrowserLauncher
import kotlin.io.path.Path

/**
 * Attempts to create a [BrowserLauncher] from fixed choices: `default` or `none`.
 * @param input the input string representing the browser choice
 * @return the corresponding [BrowserLauncher], if any
 */
private fun fromFixedChoices(input: String): BrowserLauncher? =
    when (input) {
        "default" -> DefaultBrowserLauncher()
        "none" -> NoneBrowserLauncher()
        else -> null
    }

/**
 * Attempts to create a [BrowserLauncher] from environment variables (e.g. `chrome` -> `BROWSER_CHROME`).
 * @param input the input string representing the browser choice (e.g. `chrome`, `firefox`)
 * @param envLookup function to look up environment variable values.
 *                  If different from `System::getenv`, it can be used for testing purposes
 * @return the corresponding [BrowserLauncher], if any
 */
private fun fromEnv(
    input: String,
    envLookup: (String) -> String?,
): BrowserLauncher? =
    EnvBrowserLauncher(input, envLookup)
        .takeIf { it.isValid }
        ?.also { Log.info("Using browser launcher $input (env ${it.envName})") }

/**
 * Attempts to create a [BrowserLauncher] from a given file system path.
 * @param input the input string representing the file system path to the browser executable
 * @return the corresponding [BrowserLauncher], if any
 */
private fun fromPath(input: String): BrowserLauncher? =
    PathBrowserLauncher(Path(input))
        .takeIf { it.isValid }
        ?.also { Log.info("Using browser launcher from path: $input") }

/**
 * Option to select a browser launcher from the CLI,
 * with validation and support of selection by name, path, or fixed choices.
 * @param default the default browser launcher to use if no choice is made
 * @param shouldValidate whether the choice should be validated
 * @param envLookup function to look up environment variable values.
 *                  If different from `System::getenv`, it can be used for testing purposes
 */
fun CliktCommand.browserLauncherOption(
    default: BrowserLauncher = NoneBrowserLauncher(),
    shouldValidate: () -> Boolean = { true },
    envLookup: (String) -> String? = System::getenv,
): OptionDelegate<out BrowserLauncher?> =
    option(
        "-b",
        "--browser",
        help = "Browser to open the served file in (name, path, 'default', 'none')",
    ).convert { input ->
        val caseInsensitiveInput = input.lowercase()
        val launcher =
            fromFixedChoices(caseInsensitiveInput)
                ?: fromEnv(caseInsensitiveInput, envLookup)
                ?: fromPath(input)

        require(!shouldValidate() || launcher != null) {
            "The specified browser ($input) cannot be launched " +
                "because it is either not installed, not loaded in the environment (BROWSER_<NAME>), " +
                "not executable, or unsupported."
        }

        launcher ?: default
    }.default(default)
