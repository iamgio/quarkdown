package com.quarkdown.interaction

/**
 * Environment variables that may affect the `interaction` module.
 */
object Env {
    /**
     * The path to the Chromium-family browser executable used for PDF export.
     */
    const val QUARKDOWN_CHROME_PATH = "QD_CHROME_PATH"

    /**
     * Whether to disable the Chrome sandbox for PDF export.
     */
    const val NO_SANDBOX = "QD_NO_SANDBOX"

    /**
     * @return the value of the [key] environment variable, or `null` if it is unset or blank
     */
    private operator fun get(key: String): String? = System.getenv(key)?.takeIf { it.isNotBlank() }

    /**
     * The path to the Chromium-family browser executable used for PDF export, if set.
     * This is determined by the `QD_CHROME_PATH` environment variable.
     */
    val chromePath: String?
        get() = this[QUARKDOWN_CHROME_PATH]
}
