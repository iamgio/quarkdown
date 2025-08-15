package com.quarkdown.cli.server

import com.github.ajalt.clikt.parameters.options.OptionWithValues
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.choice
import com.quarkdown.server.browser.BrowserLauncher
import com.quarkdown.server.browser.DefaultBrowserLauncher
import com.quarkdown.server.browser.NoneBrowserLauncher
import com.quarkdown.server.browser.SimpleEnvBrowserLauncher

/**
 * Utility to provide browser launchers for the CLI.
 */
internal object BrowserLaunchers {
    object Chrome : SimpleEnvBrowserLauncher("chrome")

    object Chromium : SimpleEnvBrowserLauncher("chromium")

    object Edge : SimpleEnvBrowserLauncher("edge")

    object Firefox : SimpleEnvBrowserLauncher("firefox")

    private val choices: Map<String, BrowserLauncher> =
        mapOf(
            "default" to DefaultBrowserLauncher(),
            "none" to NoneBrowserLauncher(),
            "chrome" to Chrome,
            "edge" to Edge,
            "firefox" to Firefox,
            "chromium" to Chromium,
        )

    /**
     * Provides a validated choice of browser launchers for the CLI.
     */
    fun OptionWithValues<String?, String, String>.browserChoice(default: BrowserLauncher? = null) =
        choice(choices, ignoreCase = true)
            .run { default?.let { default(it) } ?: this }
            .validate {
                require(it.isValid) {
                    "The specified browser (${it::class.simpleName}) cannot be launched " +
                        "because it is either not installed, not loaded in the environment (BROWSER_<NAME>), or unsupported."
                }
            }
}
