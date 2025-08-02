package com.quarkdown.interaction

/**
 * Environment variables that may affect the `interaction` module.
 */
object Env {
    /**
     * The prefix for NPM operations used by Quarkdown.
     */
    const val QUARKDOWN_NPM_PREFIX = "QD_NPM_PREFIX"
    private const val NODE_PATH = "NODE_PATH"

    /**
     * Whether to disable the Chrome sandbox for PDF export.
     */
    const val NO_SANDBOX = "QD_NO_SANDBOX"

    private operator fun get(key: String): String? = System.getenv(key)

    /**
     * The global prefix for NPM operations, if set.
     * This is determined by the `QD_NPM_PREFIX` environment variable.
     */
    val npmPrefix: String?
        get() = this[QUARKDOWN_NPM_PREFIX]

    /**
     * The path to the Node.js modules, if set. Ideally, this should point to `${QD_NPM_PREFIX}/node_modules`.
     * This is determined by the `NODE_PATH` environment variable.
     */
    val nodePath: String?
        get() = this[NODE_PATH]
}
