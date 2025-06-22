package com.quarkdown.interaction.executable

/**
 * Environment variables that may affect the `interaction` module.
 */
object Env {
    private const val NPM_GLOBAL_PREFIX = "NPM_GLOBAL_PREFIX"
    private const val NODE_PATH = "NODE_PATH"

    private operator fun get(key: String): String? = System.getenv(key)

    /**
     * The global prefix for NPM operations, if set.
     * This is determined by the `NPM_GLOBAL_PREFIX` environment variable.
     */
    val npmGlobalPrefix: String?
        get() = this[NPM_GLOBAL_PREFIX]

    /**
     * The path to the Node.js modules, if set. Ideally, this should point to `${NPM_GLOBAL_PREFIX}/node_modules`.
     * This is determined by the `NODE_PATH` environment variable.
     */
    val nodePath: String?
        get() = this[NODE_PATH]
}
