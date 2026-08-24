package com.quarkdown.interaction

import java.io.File

/**
 * Name of the directory npm installs modules into, relative to a prefix.
 */
private const val NODE_MODULES_DIR = "node_modules"

/**
 * Environment variables that may affect the `interaction` module.
 */
object Env {
    /**
     * The prefix for NPM operations used by Quarkdown.
     */
    const val QUARKDOWN_NPM_PREFIX = "QD_NPM_PREFIX"

    /**
     * The path Node.js resolves modules from.
     */
    const val NODE_PATH = "NODE_PATH"

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
     * The path to the Node.js modules, if it can be determined.
     *
     * An explicitly set `NODE_PATH` wins. Otherwise it is derived from [npmPrefix] by appending
     * `node_modules`, mirroring the layout `npm install --prefix` produces. Deriving it here,
     * rather than expecting a launcher script to export it, keeps module resolution working when
     * Quarkdown runs as a native binary.
     *
     * @see nodePathFrom
     */
    val nodePath: String?
        get() = nodePathFrom(this[NODE_PATH], npmPrefix)

    /**
     * Resolves Node's module path from its two possible sources.
     * Blank values are treated as unset.
     *
     * @param explicitNodePath value of the `NODE_PATH` environment variable, if any
     * @param npmPrefix value of the `QD_NPM_PREFIX` environment variable, if any
     * @return the resolved module path, or `null` if neither source yields one
     */
    internal fun nodePathFrom(
        explicitNodePath: String?,
        npmPrefix: String?,
    ): String? =
        explicitNodePath?.takeIf { it.isNotBlank() }
            ?: npmPrefix?.takeIf { it.isNotBlank() }?.let { File(it, NODE_MODULES_DIR).path }
}
